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
package com.camline.projects.smardes.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.el.ExpressionEngine;
import com.camline.projects.smardes.dq.DataQueryFunctions;
import com.camline.projects.smardes.jsonapi.SmarDesException;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.jsonapi.el.JSONFunctions;
import com.camline.projects.smartdev.ruledef.AssignNameValueExpressionType;
import com.camline.projects.smartdev.ruledef.ConditionalNameValueExpressionType;
import com.camline.projects.smartdev.ruledef.NameValueExpressionType;
import com.camline.projects.smartdev.ruledef.SetELContextType;

public class ExpressionHandler extends ExpressionEngine {
	private static final Logger logger = LoggerFactory.getLogger(ExpressionHandler.class);

	/**
	 * Refers to global variables that can be defined initially
	 */
	public static final String GLOBAL_VARIABLES = "global";

	/**
	 * Refers to conversation context repository; always available
	 * Type is {@link com.camline.projects.smardes.rule.ConversationContextRepository.ConversationContextRepository}
	 */
	public static final String CONVERSATION_CONTEXT_REPOSITORY = "cc";

	/**
	 * Refers to the current message object; always available
	 * Type is {@link ELMessage}
	 */
	public static final String CURRENT_MESSAGE_OBJECT = "msg";

	/**
	 * Refers to address/queue where the current message came in
	 * Type is {@link ELMessage}
	 */
	public static final String INCOMING_ADDRESS = "incomingAddress";


	public ExpressionHandler() {
		registerFunctions(JSONFunctions.class, DataQueryFunctions.class);
	}

	public void setupMessageContext(final RuleEngineContext ruleEngineContext, final ELMessage elMessage, String address) {
		setContextVariable(GLOBAL_VARIABLES, ruleEngineContext.getGlobalVariables());
		setContextVariable(CONVERSATION_CONTEXT_REPOSITORY,
				ruleEngineContext.getConversationContextRepository());
		setContextVariable(CURRENT_MESSAGE_OBJECT, elMessage);
		setContextVariable(INCOMING_ADDRESS, address);
	}

	public Object extractValue(final NameValueExpressionType nameValueExpressionType) {
		return extractValue(nameValueExpressionType.getName(), nameValueExpressionType.getValueAttribute(),
				nameValueExpressionType.getExpression(), nameValueExpressionType.getValue());
	}

	public Object extractValue(final String name, final String valueAttribute, final String expressionAttribute,
			final String expressionContent) {
		if (valueAttribute != null ^ expressionAttribute != null
				^ StringUtils.isNotEmpty(expressionContent)) {
			if (valueAttribute != null) {
				return valueAttribute;
			}
			return evaluateExpression(expressionAttribute != null ? expressionAttribute : expressionContent);
		}
		throw new ConfigurationException(
				"Name-Value-Expression type with name '" + name + "' must have either 'value' or 'expression' set.");
	}

	public void patchValues(final SetELContextType patchMessageBody, final Object patchMap,
			final NamedConditions namedConditions) {
		if (patchMessageBody == null) {
			return;
		}

		final String bodyVariable = patchMessageBody.getVariable();
		try {
			setContextVariable(bodyVariable, patchMap);
			patchMessageBody.getProperty()
					.forEach(property -> patchValue(bodyVariable, property, namedConditions));
		} finally {
			removeContextVariable(bodyVariable);
		}
	}

	public void patchValues(final String bodyVariable, final List<ConditionalNameValueExpressionType> properties,
			NamedConditions namedConditions) {
		properties.forEach(property -> patchValue(bodyVariable, property, namedConditions));
	}

	private void patchValue(final String bodyVariable, final ConditionalNameValueExpressionType property,
			final NamedConditions namedConditions) {
		if (namedConditions.conditionFails(property.getCondition())) {
			logger.debug("Skip conditional patch property {}", property.getName());
			return;
		}

		patchValue(bodyVariable, property, property.isOverwrite());
	}

	/**
	 * Set a value based on property assuming that bodyVariable is already in the context
	 *
	 * @param bodyVariable
	 * @param property
	 */
	public void patchValue(final String bodyVariable, final AssignNameValueExpressionType property) {
		patchValue(bodyVariable, property, true);
	}

	private void patchValue(final String bodyVariable, final AssignNameValueExpressionType property,
			boolean overwrite) {
		String propertyName;
		if (property.getKey() != null) {
			propertyName = property.getName() + "[" + property.getKey() + "]";
		} else {
			propertyName = property.getName();
		}

		if (!propertyName.startsWith(bodyVariable + ".") && !propertyName.startsWith(bodyVariable + "[")) {
			logger.warn("Patching is only allowed for the current buffer {}, but expression is {}",
					bodyVariable, propertyName);
		}

		boolean doOverwrite;
		if (!overwrite) {
			final Object value = evaluateExpression(propertyName);
			doOverwrite = value == null;
		} else {
			doOverwrite = overwrite;
		}

		if (doOverwrite) {
			final Object value = extractValue(property);
			logger.info("Patching {}{}={}", property.getExpression() != null ? "expr. " : "value ", propertyName,
					value);
			setValue(propertyName, value);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> resolveIterateOver(final String iterateOver) {
		if (iterateOver == null) {
			/*
			 * Special case: if iterateOver is null then we assume that
			 * the address is set fixed. Therefore we return a List with one null.
			 */
			return Arrays.asList((T)null);
		}

		final Object result = evaluateExpression(iterateOver);

		if (result instanceof List) {
			return (List<T>) result;
		}
		if (result instanceof Collection) {
			return (List<T>) ((Collection<?>)result).stream().collect(Collectors.toList());
		}
		if (result instanceof String || result instanceof Number) {
			return (List<T>) Arrays.asList(result);
		}
		throw new SmarDesException("iteratorOver expression returned an incompatible result: " + result);
	}
}
