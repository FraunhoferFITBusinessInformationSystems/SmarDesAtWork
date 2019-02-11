/*******************************************************************************
 * Copyright (C) 2018-2019 camLine GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.camline.projects.smardes.rule.actions;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.jms.JMSClient;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.SmarDesException;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.rule.ExpressionHandler;
import com.camline.projects.smardes.rule.NamedConditions;
import com.camline.projects.smardes.rule.RuleGroupContext;
import com.camline.projects.smartdev.ruledef.BaseVisitor;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType.BaseMessage;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType.BaseMessage.PatchMessageBodyRef;
import com.camline.projects.smartdev.ruledef.ConditionalBroadcastMessageType;
import com.camline.projects.smartdev.ruledef.NameValueExpressionType;
import com.camline.projects.smartdev.ruledef.SetELContextType;
import com.camline.projects.smartdev.ruledef.Visitable;

public abstract class RuleActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(RuleActionHandler.class);

	protected final RuleGroupContext ruleGroupContext;
	protected final ExpressionHandler expressionHandler;

	public RuleActionHandler(final RuleGroupContext ruleGroupContext) {
		this.ruleGroupContext = ruleGroupContext;
		this.expressionHandler = ruleGroupContext.getExpressionHandler();
	}

	protected void broadcastMessagesConditionally(
			final List<ConditionalBroadcastMessageType> conditionalBroadcastMessages, final String messageType,
			final NamedConditions namedConditions) {
		for (final ConditionalBroadcastMessageType conditionalBroadcastMessage : conditionalBroadcastMessages) {
			final String condition = conditionalBroadcastMessage.getCondition();
			if (namedConditions.conditionFails(condition)) {
				logger.info("Skip {} to address {} because condition '{}' is not fulfilled",
						messageType, conditionalBroadcastMessage.getAddress(), condition);
				continue;
			}
			logger.info("Broadcast {} to address {}", messageType, conditionalBroadcastMessage.getAddress());
			broadcastMessage(conditionalBroadcastMessage, namedConditions);
		}
	}

	protected int broadcastMessage(final BroadcastMessageType broadcastMessage,
			final NamedConditions namedConditions) {
		final List<?> iteratorVars = expressionHandler.resolveIterateOver(broadcastMessage.getIterateOver());
		for (final Object iteratorVar : iteratorVars) {
			expressionHandler.setContextVariable(broadcastMessage.getIteratorVar(), iteratorVar);
			pushMessage(broadcastMessage, namedConditions);
		}
		return iteratorVars.size();
	}

	protected void pushMessage(final BroadcastMessageType pushMessage, final NamedConditions namedConditions) {
		try {
			final String address = expressionHandler.extractValue("address", null, pushMessage.getAddress(), null).toString();

			final BytesMessage message = ruleGroupContext.getRuleEngineContext().getJmsContextOutgoing()
					.createBytesMessage();

			handleBaseMessage(pushMessage.getBaseMessage(), message, namedConditions);

			if (pushMessage.getExtraApplicationProperties() != null) {
				for (final NameValueExpressionType property : pushMessage.getExtraApplicationProperties().getProperty()) {
					message.setObjectProperty(property.getName(), expressionHandler.extractValue(property));
				}
			}

			if (pushMessage.getSubject() != null) {
				message.setJMSType(pushMessage.getSubject());
			}

			long deliveryDelay = pushMessage.getDeliveryDelay() != null ?
					expressionHandler.evaluateLongExpression(pushMessage.getDeliveryDelay()) : 0;

			sendAndStoreResponse(pushMessage, address, message, deliveryDelay);
		} catch (final JMSException e) {
			logger.error("Error sending message to bus", e);
		}

	}

	private void sendAndStoreResponse(final BroadcastMessageType pushMessage, final String address,
			final BytesMessage message, long deliveryDelay) throws JMSException {
		boolean storeResponse = pushMessage.getResponse() != null;
		try (JMSClient jmsClient = new JMSClient(ruleGroupContext.getRuleEngineContext().getJmsContextOutgoing(),
				address, storeResponse)) {
			if (storeResponse) {
				Message response;
				if (pushMessage.getResponse().getTimeout() != null) {
					response = jmsClient.sendReceiveMessage(message, deliveryDelay,
							pushMessage.getResponse().getTimeout().longValue());
				} else {
					response = jmsClient.sendReceiveMessage(message, deliveryDelay);
				}
				final ELMessage elMessage = new ELMessage(response, true);
				expressionHandler.setContextVariable(pushMessage.getResponse().getResponseVar(), elMessage);
			} else {
				jmsClient.sendMessage(message, deliveryDelay);
			}
		}
	}

	private void handleBaseMessage(final BaseMessage baseMessage, final BytesMessage message,
			final NamedConditions namedConditions) throws JMSException {
		if (baseMessage == null) {
			return;
		}

		Object baseMessageObject = expressionHandler.evaluateExpression(baseMessage.getExpression());
		final ELMessage srcMessage = mapToELMessage(baseMessageObject, message);

		if (srcMessage != null) {
			/*
			 * Copy subject
			 */
			message.setJMSType(srcMessage.getRaw().getJMSType());

			/*
			 * Copy message properties
			 */
			final String prefix = defaultString(baseMessage.getPrefixApplicationProperties());
			for (final Entry<String, Object> propertyEntry : srcMessage.getProp().entrySet()) {
				message.setObjectProperty(prefix + propertyEntry.getKey(), propertyEntry.getValue());
			}
		}

		/*
		 * Unmarshal body in a map, patch it and marshal it in a new message
		 */
		if (baseMessage.getPatchMessageBodyOrPatchMessageBodyRef().isEmpty()) {
			if (srcMessage != null) {
				message.writeBytes(JSONB.instance().marshal(srcMessage.getBody()));
			}
		} else {
			final Map<String, Object> messageBodyMap = createNewBodyMap(srcMessage);

			PatchMessageBodyVisitor patchMessageBodyVisitor = new PatchMessageBodyVisitor(ruleGroupContext,
					messageBodyMap, namedConditions);
			for (final Object patchMessageChoice : baseMessage.getPatchMessageBodyOrPatchMessageBodyRef()) {
				final Boolean visitorResult = ((Visitable)patchMessageChoice).accept(patchMessageBodyVisitor);
				if (visitorResult != Boolean.TRUE) {
					logger.warn("Unhandled patchMessageBodyElement {}", patchMessageChoice.getClass());
				}
			}

			message.writeBytes(JSONB.instance().marshal(messageBodyMap));
		}

	}

	private static ELMessage mapToELMessage(final Object baseMessageObject, final BytesMessage message) {
		final ELMessage srcMessage;
		if (baseMessageObject instanceof ELMessage) {
			srcMessage = (ELMessage)baseMessageObject;
		} else if (baseMessageObject instanceof Map<?, ?>){
			srcMessage = new ELMessage(message, (Map<?, ?>)baseMessageObject);
		} else if (baseMessageObject == null) {
			srcMessage = null;
		} else {
			throw new SmarDesException("Incompatible base message object type " + baseMessageObject.getClass());
		}
		return srcMessage;
	}

	private static Map<String, Object> createNewBodyMap(final ELMessage srcMessage) {
		if (srcMessage == null) {
			return new HashMap<>();
		}
		final Map<String, Object> bodyMap = JSONB.instance().unmarshalBodyGeneric(srcMessage.getBodyText());
		return bodyMap != null ? bodyMap : new HashMap<>();
	}

	static class PatchMessageBodyVisitor extends BaseVisitor<Boolean, RuntimeException> {
		private final RuleGroupContext ruleGroupContext;
		private final Map<String, Object> messageBodyMap;
		private final NamedConditions namedConditions;

		PatchMessageBodyVisitor(RuleGroupContext ruleGroupContext, Map<String, Object> messageBodyMap,
				NamedConditions namedConditions) {
			this.ruleGroupContext = ruleGroupContext;
			this.messageBodyMap = messageBodyMap;
			this.namedConditions = namedConditions;
		}

		@Override
		public Boolean visit(final SetELContextType patchMessageBody) {
			ruleGroupContext.getExpressionHandler().patchValues(patchMessageBody, messageBodyMap,
					namedConditions);
			return Boolean.TRUE;
		}

		@Override
		public Boolean visit(final PatchMessageBodyRef patchMessageBodyRef) {
			final SetELContextType patchMessageBody = ruleGroupContext.getRuleEngineContext()
					.getPatchMessageBodyPrototype(patchMessageBodyRef.getName());
			if (patchMessageBody != null) {
				visit(patchMessageBody);
			} else {
				throw new SmarDesException(
						String.format("patchMessageBodyPrototype with name '%s' does not exist",
								patchMessageBodyRef.getName()));
			}
			return Boolean.TRUE;
		}
	}
}
