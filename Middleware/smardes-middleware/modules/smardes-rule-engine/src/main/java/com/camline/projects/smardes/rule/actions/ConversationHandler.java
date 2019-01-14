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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.rule.ConversationContext;
import com.camline.projects.smardes.rule.ConversationContextRepository;
import com.camline.projects.smardes.rule.ConversationManager;
import com.camline.projects.smardes.rule.ConversationManager.State;
import com.camline.projects.smardes.rule.NamedConditions;
import com.camline.projects.smardes.rule.RuleGroupContext;
import com.camline.projects.smartdev.ruledef.ConditionalBroadcastMessageType;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.AcceptConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.CloseConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.ContinueConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation.ConversationMessage;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation.ExecuteRule;
import com.camline.projects.smartdev.ruledef.SetELContextType;

public class ConversationHandler extends RuleActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConversationHandler.class);

	private static final int SECONDS_PER_HOUR = 3600;

	private final ConversationContextRepository conversationContextRepository;
	private final ScheduledExecutorService conversationMonitor;

	public ConversationHandler(final RuleGroupContext ruleGroupContext) {
		super(ruleGroupContext);
		this.conversationContextRepository = ruleGroupContext.getRuleEngineContext().getConversationContextRepository();
		this.conversationMonitor = ruleGroupContext.getRuleEngineContext().getConversationMonitor();
	}

	public void startConversation(final StartConversation startConversation, final ELMessage message,
			final NamedConditions namedConditions) {
		final ConversationContext conversationContext = conversationContextRepository.createContext(message,
				startConversation, namedConditions);

		logger.info("Start conversation {} with uuid {}", startConversation.getName(), conversationContext.getUuid());

		// We might use this in the next EL computations
		expressionHandler.setContextVariable(startConversation.getVariable(), conversationContext);

		expressionHandler.patchValues(startConversation.getSetConversationContext(), conversationContext.getCtx(),
				conversationContext.getNamedConditions());

		JSONB.instance().dumpObject("EL Context Variables after patching", expressionHandler.getContextVariables());

		conversationMonitor.schedule(() -> expireConversationContext(conversationContext),
				(long) (startConversation.getExpiry() * SECONDS_PER_HOUR), TimeUnit.SECONDS);

		if (startConversation.getRequestMessage() != null) {
			final List<String> users = expressionHandler
					.resolveIterateOver(startConversation.getRequestMessage().getIterateOver());
			logger.info("Conversation user list is {}", users);
			conversationContext.getConversationManager().setUsers(users);

			if (!conversationContext.getConversationManager().isParallelMode()) {
				sendMessageAndMonitor(conversationContext, null);
			} else {
				broadcastMessage(startConversation.getRequestMessage(),
						conversationContext.getNamedConditions());
				monitorConversation(conversationContext, users);
			}
		}

		broadcastMessagesConditionally(startConversation.getPushMessage(), "pushMessage",
				conversationContext.getNamedConditions());
	}

	private void expireConversationContext(final ConversationContext cc) {
		logger.info("Expire context with id {} and state {}", cc.getId(), cc.getState());
		boolean didClose = cc.getConversationManager().tryExpire();
		if (didClose) {
			feedbackConversation(cc, null, cc.getStartConversation().getExpiredMessage(),
					ConversationMessage::isExpired, "expiredMessage");
			executeInternalRules(cc.getNamedConditions(), cc.getStartConversation().getExecuteRule(),
					ExecuteRule::isExpired);
		}
		conversationContextRepository.remove(cc.getUuid());
		logger.info("{} conversations left in repository", Integer.valueOf(conversationContextRepository.size()));
	}

	public void continueConversation(final ContinueConversation continueConversation) {
		final String conversationId = expressionHandler.evaluateExpression(continueConversation.getId(), String.class);
		final UUID uuid = UUID.fromString(conversationId);
		final ConversationContext conversationContext = conversationContextRepository.get(uuid);

		if (conversationContext == null) {
			logger.warn("Unknown conversation id {}. Cannot continue...", uuid);
			return;
		}
		logger.info("Continue conversation {} with uuid {}", conversationContext.getName(),
				conversationContext.getUuid());

		String userId = resolveUserId(continueConversation.getUserId());

		if (!conversationContext.getConversationManager().isParallelMode()) {
			expressionHandler.patchValues(continueConversation.getSetConversationContext(), conversationContext.getCtx(),
					conversationContext.getNamedConditions());
			sendMessageAndMonitor(conversationContext, null);
		}

		final boolean stateChanged = conversationContext.getConversationManager().reject(userId);
		if (stateChanged) {
			feedbackConversation(conversationContext, continueConversation.getSetConversationContext(),
					conversationContext.getStartConversation().getRejectedMessage(), ConversationMessage::isRejected,
					"rejectedMessage");
			executeInternalRules(conversationContext.getNamedConditions(),
					conversationContext.getStartConversation().getExecuteRule(), ExecuteRule::isRejected);
		}
	}

	public void acceptConversation(final AcceptConversation acceptConversation) {
		final String conversationId = expressionHandler.evaluateExpression(acceptConversation.getId(), String.class);
		final UUID uuid = UUID.fromString(conversationId);
		final ConversationContext conversationContext = conversationContextRepository.get(uuid);

		if (conversationContext == null) {
			logger.warn("Unknown conversation id {}. Cannot accept...", uuid);
			return;
		}
		logger.info("Accept conversation {} with uuid {}", conversationContext.getName(),
				conversationContext.getUuid());

		String userId = resolveUserId(acceptConversation.getUserId());

		final boolean stateChanged = conversationContext.getConversationManager().accept(userId);
		if (stateChanged) {
			feedbackConversation(conversationContext, acceptConversation.getSetConversationContext(),
					conversationContext.getStartConversation().getAcceptedMessage(), ConversationMessage::isAccepted,
					"acceptedMessage");
			executeInternalRules(conversationContext.getNamedConditions(),
					conversationContext.getStartConversation().getExecuteRule(), ExecuteRule::isAccepted);
		}
	}

	private String resolveUserId(String userId) {
		return userId != null ? expressionHandler.evaluateExpression(userId) : null;
	}

	private void executeInternalRules(NamedConditions namedConditions, List<ExecuteRule> executeRules,
			final Predicate<? super ExecuteRule> filter) {
		for (ExecuteRule executeRule : executeRules) {
			if (!filter.test(executeRule)) {
				continue;
			}

			final String condition = executeRule.getCondition();
			if (namedConditions.conditionFails(condition)) {
				logger.info("Skip executing internal rule {} because condition '{}' is not fulfilled",
						executeRule.getName(), condition);
				continue;
			}

			ruleGroupContext.executeInternalRule(executeRule);
		}
	}

	public void closeConversation(final CloseConversation closeConversation) {
		final String conversationId = expressionHandler.evaluateExpression(closeConversation.getId(), String.class);
		final UUID uuid = UUID.fromString(conversationId);
		final ConversationContext conversationContext = conversationContextRepository.get(uuid);

		if (conversationContext == null) {
			logger.warn("Unknown conversation id {}. Cannot close...", uuid);
			return;
		}
		logger.info("Close conversation {} with uuid {}", conversationContext.getName(), conversationContext.getUuid());

		final boolean stateChanged = conversationContext.getConversationManager().tryClose();
		if (stateChanged) {
			feedbackConversation(conversationContext, closeConversation.getSetConversationContext(),
					conversationContext.getStartConversation().getClosedMessage(), ConversationMessage::isClosed,
					"closedMessage");
		} else {
			logger.warn("Conversation {} is already closed.", conversationContext.getId());
		}
	}

	private void feedbackConversation(final ConversationContext conversationContext,
			final SetELContextType patchContext,
			final List<ConditionalBroadcastMessageType> explicitConversationMessages,
			final Predicate<? super ConversationMessage> filter, final String conversationMessageType) {
		if (patchContext != null) {
			expressionHandler.patchValues(patchContext, conversationContext.getCtx(),
					conversationContext.getNamedConditions());
		}

		expressionHandler.setContextVariable(conversationContext.getStartConversation().getVariable(),
				conversationContext);

		List<ConditionalBroadcastMessageType> conversationMessages = conversationContext.getStartConversation()
				.getConversationMessage().stream().filter(filter)
				.collect(Collectors.toList());
		conversationMessages.addAll(explicitConversationMessages);

		broadcastMessagesConditionally(conversationMessages, conversationMessageType,
				conversationContext.getNamedConditions());
	}

	private void sendMessageAndMonitor(final ConversationContext conversationContext, final String lastAssignee) {
		final ConversationManager conversationManager = conversationContext.getConversationManager();
		final Triple<State, String, Boolean> result = conversationManager.nextAssignee(lastAssignee);
		final State state = result.getLeft();
		final String assignee = result.getMiddle();
		final Boolean stateChanged = result.getRight();

		final StartConversation startConversation = conversationContext.getStartConversation();
		expressionHandler.setContextVariable(startConversation.getVariable(), conversationContext);

		if (state != State.PENDING) {
			if (stateChanged.booleanValue()) {
				if (state == State.REJECTED) {
					feedbackConversation(conversationContext, null, startConversation.getRejectedMessage(),
							ConversationMessage::isRejected, "rejectedMessage");
				}
			} else {
				logger.info("Conversation already finished with status {}", state);
			}
			return;
		}

		if (assignee == null) {
			logger.warn("No next assignee. Somebody else was assigned in between.");
			return;
		}

		expressionHandler.setContextVariable(startConversation.getRequestMessage().getIteratorVar(), assignee);
		pushMessage(startConversation.getRequestMessage(), conversationContext.getNamedConditions());

		final long timeout = expressionHandler
				.evaluateLongExpression(startConversation.getRequestMessage().getTimeout());
		final ScheduledFuture<?> future = conversationMonitor.schedule(() -> {
			logger.info("Assignee {} didn't answer within given time frame. Treat as REJECTED.", assignee);
			try {
				sendMessageAndMonitor(conversationContext, assignee);
			} catch (final RuntimeException e) {
				logger.error("Error in sending rejected message", e);
			}
		}, timeout, TimeUnit.SECONDS);
		conversationManager.registerTimeoutHandler(assignee, future);
	}

	private void monitorConversation(final ConversationContext conversationContext, final List<String> users) {
		ConversationManager conversationManager = conversationContext.getConversationManager();
		final StartConversation startConversation = conversationContext.getStartConversation();
		expressionHandler.setContextVariable(startConversation.getVariable(), conversationContext);

		final long timeout = expressionHandler
				.evaluateLongExpression(startConversation.getRequestMessage().getTimeout());
		final ScheduledFuture<?> future = conversationMonitor.schedule(() -> {
			logger.info("Assignees {} didn't answer within given time frame. Treat as REJECTED.",
					users);
			try {
				sendMessageAndMonitor(conversationContext, null);
			} catch (final RuntimeException e) {
				logger.error("Error in sending rejected message", e);
			}
		}, timeout, TimeUnit.SECONDS);
		conversationManager.registerTimeoutHandler("dummy", future);
	}
}
