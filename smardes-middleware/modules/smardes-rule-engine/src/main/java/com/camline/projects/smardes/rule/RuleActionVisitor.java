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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.rule.actions.ConversationHandler;
import com.camline.projects.smardes.rule.actions.DumpFileHandler;
import com.camline.projects.smardes.rule.actions.PushMessageHandler;
import com.camline.projects.smardes.rule.api.ActionResult;
import com.camline.projects.smartdev.ruledef.BaseVisitor;
import com.camline.projects.smartdev.ruledef.ConditionalBroadcastMessageType;
import com.camline.projects.smartdev.ruledef.ExecuteRuleType;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.AcceptConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.CloseConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.ContinueConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.DumpFile;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.SetGlobalVariables;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.SetLocalVariables;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation;

class RuleActionVisitor extends BaseVisitor<Boolean, RuntimeException> {
	private static final String PUSH_MESSAGE_ACTION_NAME = "PushMessage";

	private static final Logger logger = LoggerFactory.getLogger(RuleActionVisitor.class);

	private final RuleGroupContext ruleGroupContext;
	private final ExpressionHandler expressionHandler;
	private final List<Pair<String, Exception>> executedActions;
	private final NamedConditions namedConditions;

	RuleActionVisitor(final RuleGroupContext ruleGroupContext, final String ruleName,
			final NamedConditions namedConditions) {
		this.ruleGroupContext = ruleGroupContext;
		this.expressionHandler = ruleGroupContext.getExpressionHandler();
		this.namedConditions = namedConditions;
		this.executedActions = new ArrayList<>();

		logger.info("Execute actions for rule '{}'", ruleName);
	}

	private boolean conditionFails(final String condition, final String actionName) {
		if (namedConditions.conditionFails(condition)) {
			logger.info("Skip {} action because condition '{}' is not fulfilled", actionName, condition);
			return true;
		}
		return false;
	}

	@Override
	public Boolean visit(SetGlobalVariables setGlobalVariables) {
		final String actionName = ActionResult.createActionName(SetGlobalVariables.class,
				Integer.valueOf(setGlobalVariables.getProperty().size()));
		if (conditionFails(setGlobalVariables.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		try {
			expressionHandler.patchValues(ExpressionHandler.GLOBAL_VARIABLES, setGlobalVariables.getProperty(),
					namedConditions);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(SetLocalVariables setLocalVariables) {
		final String actionName = ActionResult.createActionName(SetLocalVariables.class,
				Integer.valueOf(setLocalVariables.getProperty().size()));
		if (conditionFails(setLocalVariables.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		String baseLocalVar = setLocalVariables.getVariable();
		if (!expressionHandler.existsContextVariable(baseLocalVar)) {
			expressionHandler.setContextVariable(baseLocalVar, new HashMap<>());
		}

		try {
			expressionHandler.patchValues(baseLocalVar, setLocalVariables.getProperty(), namedConditions);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(final DumpFile dumpFile) {
		final String actionName = ActionResult.createActionName(DumpFile.class, dumpFile.getTemplateName());
		if (conditionFails(dumpFile.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		try {
			final DumpFileHandler handler = new DumpFileHandler(ruleGroupContext);
			handler.execute(dumpFile);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(final ConditionalBroadcastMessageType pushMessage) {
		if (conditionFails(pushMessage.getCondition(), PUSH_MESSAGE_ACTION_NAME)) {
			return Boolean.TRUE;
		}

		try {
			final PushMessageHandler handler = new PushMessageHandler(ruleGroupContext);
			int numMessages = handler.execute(pushMessage, namedConditions);
			String actionName = ActionResult.createActionName(PUSH_MESSAGE_ACTION_NAME, Integer.valueOf(numMessages));
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(PUSH_MESSAGE_ACTION_NAME, e);
		}
	}

	@Override
	public Boolean visit(final StartConversation startConversation) {
		final String actionName = ActionResult.createActionName(StartConversation.class, startConversation.getName());
		if (conditionFails(startConversation.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		try {
			final ConversationHandler handler = new ConversationHandler(ruleGroupContext);
			handler.startConversation(startConversation, ruleGroupContext.getElMessage(), namedConditions);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(final AcceptConversation acceptConversation) {
		final String actionName = ActionResult.createActionName(AcceptConversation.class);
		if (conditionFails(acceptConversation.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		try {
			final ConversationHandler handler = new ConversationHandler(ruleGroupContext);
			handler.acceptConversation(acceptConversation);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(final CloseConversation closeConversation) {
		final String actionName = ActionResult.createActionName(CloseConversation.class);
		if (conditionFails(closeConversation.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		try {
			final ConversationHandler handler = new ConversationHandler(ruleGroupContext);
			handler.closeConversation(closeConversation);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(final ContinueConversation continueConversation) {
		final String actionName = ActionResult.createActionName(ContinueConversation.class);
		if (conditionFails(continueConversation.getCondition(), actionName)) {
			return Boolean.TRUE;
		}

		try {
			final ConversationHandler handler = new ConversationHandler(ruleGroupContext);
			handler.continueConversation(continueConversation);
			return addSuccess(actionName);
		} catch (final RuntimeException e) {
			return addError(actionName, e);
		}
	}

	@Override
	public Boolean visit(final ExecuteRuleType executeRule) {
		final String actionName = ActionResult.createActionName(ExecuteRuleType.class, executeRule.getName());
		if (conditionFails(executeRule.getCondition(), actionName)) {
			return Boolean.TRUE;
		}
		ruleGroupContext.executeInternalRule(executeRule);
		return Boolean.TRUE;
	}

	private Boolean addSuccess(String actionName) {
		executedActions.add(Pair.of(actionName, null));
		return Boolean.TRUE;
	}

	private Boolean addError(String actionName, Exception e) {
		logger.warn("{} failed with the following exception", actionName, e);
		executedActions.add(Pair.of(actionName, e));
		return Boolean.FALSE;
	}

	List<Pair<String, Exception>> getExecutedActions() {
		return executedActions;
	}
}
