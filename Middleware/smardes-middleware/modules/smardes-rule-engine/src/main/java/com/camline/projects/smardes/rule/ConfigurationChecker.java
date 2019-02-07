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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smartdev.ruledef.AssignNameValueExpressionType;
import com.camline.projects.smartdev.ruledef.BaseVisitor;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType.BaseMessage;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType.BaseMessage.PatchMessageBodyRef;
import com.camline.projects.smartdev.ruledef.BroadcastMessageType.ExtraApplicationProperties;
import com.camline.projects.smartdev.ruledef.ConditionalBroadcastMessageType;
import com.camline.projects.smartdev.ruledef.ConditionalNameValueExpressionType;
import com.camline.projects.smartdev.ruledef.ConversationActionType;
import com.camline.projects.smartdev.ruledef.ExecuteRuleType;
import com.camline.projects.smartdev.ruledef.NameValueExpressionType;
import com.camline.projects.smartdev.ruledef.RuleSetType;
import com.camline.projects.smartdev.ruledef.RuleType;
import com.camline.projects.smartdev.ruledef.RuleType.Actions;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.AcceptConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.CloseConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.ContinueConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.DumpFile;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.SetGlobalVariables;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.SetLocalVariables;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation.ConversationMessage;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation.ExecuteRule;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation.RequestMessage;
import com.camline.projects.smartdev.ruledef.RuleType.Condition;
import com.camline.projects.smartdev.ruledef.Rules;
import com.camline.projects.smartdev.ruledef.Rules.GlobalVariables;
import com.camline.projects.smartdev.ruledef.Rules.PatchMessageBodyPrototypes;
import com.camline.projects.smartdev.ruledef.Rules.PatchMessageBodyPrototypes.PatchMessageBodyPrototype;
import com.camline.projects.smartdev.ruledef.Rules.RuleGroup;
import com.camline.projects.smartdev.ruledef.Rules.RuleGroup.Selector;
import com.camline.projects.smartdev.ruledef.SetELContextType;
import com.camline.projects.smartdev.ruledef.Visitable;

class ConfigurationChecker extends BaseVisitor<Boolean, RuntimeException> {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationChecker.class);

	static final Pattern EL_VARNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

	private final Set<Pair<String, String>> globalVariableNames = new HashSet<>();
	private final Set<String> patchMessageBodyPrototypeNames = new HashSet<>();
	private final Set<String> currentRuleNamedConditions = new HashSet<>();
	private final Set<String> ruleSetNames = new HashSet<>();
	private final Set<String> ruleNames = new HashSet<>();
	private final RuleNameVisitor ruleNameVisitor = new RuleNameVisitor();

	@Override
	public Boolean visit(Rules rules) {
		boolean ok = doVisit(rules.getGlobalVariables(), rules.getPatchMessageBodyPrototypes());
		if (ok) {
			rules.accept(ruleNameVisitor);
			ok = doVisit(rules.getRuleGroup());
		}
		return Boolean.valueOf(ok);
	}

	@Override
	public Boolean visit(GlobalVariables globalVariables) {
		if (!checkBaseVar(globalVariables.getProperty(), ExpressionHandler.GLOBAL_VARIABLES)) {
			return Boolean.FALSE;
		}

		for (AssignNameValueExpressionType property : globalVariables.getProperty()) {
			boolean newEntry = globalVariableNames.add(Pair.of(property.getName(), property.getKey()));
			if (!newEntry) {
				if (property.getKey() == null) {
					logWarn("Duplicate globalVariable <property> with name '{}'", property.getName());
					return Boolean.FALSE;
				}
				logWarn("Duplicate globalVariable <property> with name and key '{}[{}]'", property.getName(), property.getKey());
				return Boolean.FALSE;
			}
		}

		return Boolean.valueOf(doVisit(globalVariables.getProperty()));
	}

	@Override
	public Boolean visit(PatchMessageBodyPrototypes patchMessageBodyPrototypes) {
		return Boolean.valueOf(doVisit(patchMessageBodyPrototypes.getPatchMessageBodyPrototype()));
	}

	@Override
	public Boolean visit(PatchMessageBodyPrototype patchMessageBodyPrototype) {
		boolean newEntry = patchMessageBodyPrototypeNames.add(patchMessageBodyPrototype.getName());
		if (!newEntry) {
			logWarn("Duplicate <patchMessageBodyPrototype> with name '{}'", patchMessageBodyPrototype.getName());
			return Boolean.FALSE;
		}
		return visit((SetELContextType) patchMessageBodyPrototype);
	}

	@Override
	public Boolean visit(RuleGroup ruleGroup) {
		boolean ok = doVisit(ruleGroup.getSelector());
		if (ok) {
			ok = doVisit(ruleGroup.getRuleOrRuleSet());
		}
		if (!ok) {
			logWarn("In rule group '{}'", ruleGroup.getName());
		}
		return Boolean.valueOf(ok);
	}

	@Override
	public Boolean visit(Selector aBean) {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(RuleSetType ruleSet) {
		boolean newEntry = ruleSetNames.add(ruleSet.getName());
		if (!newEntry) {
			logWarn("Duplicate <ruleSet> with name '{}'", ruleSet.getName());
			return Boolean.FALSE;
		}

		boolean ok = doVisit(ruleSet.getRuleOrRuleSet());
		if (!ok) {
			logWarn("In rule set '{}'", ruleSet.getName());
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(RuleType rule) {
		boolean newEntry = ruleNames.add(rule.getName());
		if (!newEntry) {
			logWarn("Duplicate <rule> with name '{}'", rule.getName());
			return Boolean.FALSE;
		}

		currentRuleNamedConditions.clear();
		boolean ok = doVisit(rule.getCondition());
		if (ok) {
			ok = doVisit(rule.getActions());
		}
		if (!ok) {
			logWarn("In rule '{}'", rule.getName());
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(Condition condition) {
		if (condition.getName() != null) {
			currentRuleNamedConditions.add(condition.getName());
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(Actions actions) {
		return Boolean.valueOf(doVisit(actions.getPushMessageOrDumpFileOrStartConversation()));
	}

	@Override
	public Boolean visit(DumpFile dumpFile) {
		if (!checkConditionName(dumpFile.getCondition())) {
			return Boolean.FALSE;
		}
		if (dumpFile.getEncoding() != null) {
			try {
				Charset.forName(dumpFile.getEncoding());
			} catch (IllegalCharsetNameException e) {
				logWarn("Unknown dumpFile encoding " + dumpFile.getEncoding(), e);
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(SetGlobalVariables setGlobalVariables) {
		if (!checkConditionName(setGlobalVariables.getCondition())) {
			return Boolean.FALSE;
		}
		if (!checkBaseVar(setGlobalVariables.getProperty(), ExpressionHandler.GLOBAL_VARIABLES)) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(doVisit(setGlobalVariables.getProperty()));
	}

	@Override
	public Boolean visit(SetLocalVariables setLocalVariables) {
		if (!checkConditionName(setLocalVariables.getCondition())) {
			return Boolean.FALSE;
		}
		if (!checkBaseVar(setLocalVariables.getProperty(), setLocalVariables.getVariable())) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(doVisit(setLocalVariables.getProperty()));
	}

	@Override
	public Boolean visit(StartConversation startConversation) {
		if (!checkConditionName(startConversation.getCondition())) {
			return Boolean.FALSE;
		}
		if (!checkVariableName(startConversation.getVariable())) {
			return Boolean.FALSE;
		}
		if (checkOrNull(startConversation.getSetConversationContext()) != Boolean.TRUE) {
			return Boolean.FALSE;
		}
		if (!doVisit(startConversation.getRequestMessage())) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(doVisit(startConversation.getPushMessage(), startConversation.getAcceptedMessage(),
				startConversation.getRejectedMessage(), startConversation.getClosedMessage(),
				startConversation.getExpiredMessage(), startConversation.getConversationMessage()));
	}

	@Override
	public Boolean visit(RequestMessage requestMessage) {
		return visit((BroadcastMessageType) requestMessage);
	}

	@Override
	public Boolean visit(AcceptConversation acceptConversation) {
		return doVisit(acceptConversation);
	}

	@Override
	public Boolean visit(ContinueConversation continueConversation) {
		return doVisit(continueConversation);
	}

	@Override
	public Boolean visit(CloseConversation closeConversation) {
		return doVisit(closeConversation);
	}

	private Boolean doVisit(ConversationActionType conversationActionType) {
		if (!checkConditionName(conversationActionType.getCondition())) {
			return Boolean.FALSE;
		}
		return checkOrNull(conversationActionType.getSetConversationContext());
	}

	private Boolean checkOrNull(SetELContextType setELContextType) {
		if (setELContextType == null) {
			return Boolean.TRUE;
		}
		return Boolean.valueOf(doVisit(setELContextType));
	}

	@Override
	public Boolean visit(ConversationMessage conversationMessage) {
		return visit((BroadcastMessageType) conversationMessage);
	}

	@Override
	public Boolean visit(ExecuteRule executeRule) {
		return visit((ExecuteRuleType) executeRule);
	}

	@Override
	public Boolean visit(ExecuteRuleType executeRule) {
		if (!ruleNameVisitor.getRules().containsKey(executeRule.getName())) {
			logger.warn("Error in rule configuration: ExecuteRule refers rule with name '{}' which doesn't exist",
					executeRule.getName());
			return Boolean.FALSE;
		}
		if (!checkConditionName(executeRule.getCondition())) {
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(ConditionalBroadcastMessageType conditionalBroadcastMessageType) {
		if (!checkConditionName(conditionalBroadcastMessageType.getCondition())) {
			return Boolean.FALSE;
		}
		return visit((BroadcastMessageType) conditionalBroadcastMessageType);
	}

	private boolean checkConditionName(String conditionName) {
		if (conditionName == null) {
			return true;
		}
		String realConditionName = conditionName.startsWith("!") ? conditionName.substring(1) : conditionName;
		if (!currentRuleNamedConditions.contains(realConditionName)) {
			logger.warn("Error in rule configuration: Named condition '{}' is not defined in current rule",
					conditionName);
			return false;
		}
		return true;
	}

	@Override
	public Boolean visit(BroadcastMessageType broadcastMessageType) {
		if (!checkVariableName(broadcastMessageType.getIteratorVar())) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(
				doVisit(broadcastMessageType.getBaseMessage(), broadcastMessageType.getExtraApplicationProperties()));
	}

	@Override
	public Boolean visit(BaseMessage baseMessage) {
		return Boolean.valueOf(doVisit(baseMessage.getPatchMessageBodyOrPatchMessageBodyRef()));
	}

	@Override
	public Boolean visit(ExtraApplicationProperties extraApplicationProperties) {
		return Boolean.valueOf(doVisit(extraApplicationProperties.getProperty()));
	}

	@Override
	public Boolean visit(PatchMessageBodyRef patchMessageBodyRef) {
		if (!patchMessageBodyPrototypeNames.contains(patchMessageBodyRef.getName())) {
			logWarn("patchMessageBodyPrototype with name '{}' does not exist", patchMessageBodyRef.getName());
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(ConditionalNameValueExpressionType conditionalNameValueExpressionType) {
		if (!checkConditionName(conditionalNameValueExpressionType.getCondition())) {
			return Boolean.FALSE;
		}
		return visit((AssignNameValueExpressionType) conditionalNameValueExpressionType);
	}

	@Override
	public Boolean visit(AssignNameValueExpressionType assignNameValueExpressionType) {
		return visit((NameValueExpressionType) assignNameValueExpressionType);
	}

	@Override
	public Boolean visit(SetELContextType setELContextType) {
		if (!checkBaseVar(setELContextType.getProperty(), setELContextType.getVariable())) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(doVisit(setELContextType.getProperty()));
	}

	private static boolean checkBaseVar(List<? extends NameValueExpressionType> properties, String baseVar) {
		if (!checkVariableName(baseVar)) {
			return false;
		}
		return properties.stream().noneMatch(property -> !expressionStartsWithVariable(property.getName(), baseVar));
	}

	private static boolean expressionStartsWithVariable(final String expression, final String elVarName) {
		boolean ok = expression.startsWith(elVarName + '.') || expression.startsWith(elVarName + '[');
		if (!ok) {
			logWarn("property/variable expression '{}' does not start with base variable '{}'", expression, elVarName);
		}
		return ok;
	}

	@Override
	public Boolean visit(NameValueExpressionType nameValueExpressionType) {
		boolean ok = nameValueExpressionType.getValueAttribute() != null
				^ nameValueExpressionType.getExpression() != null
				^ StringUtils.isNotEmpty(nameValueExpressionType.getValue());
		if (!ok) {
			logWarn("property/variable '{}' must have either value or expression set",
					nameValueExpressionType.getName());
		}
		return Boolean.valueOf(ok);
	}

	private boolean doVisit(Visitable... visitables) {
		return doVisit(Stream.of(visitables).filter(Objects::nonNull));
	}

	private boolean doVisit(List<Object> choiceElements) {
		return doVisit(choiceElements.stream().map(Visitable.class::cast));
	}

	@SafeVarargs
	private final boolean doVisit(List<? extends Visitable>... visitableLists) {
		return doVisit(Stream.of(visitableLists).flatMap(List::stream));
	}

	private boolean doVisit(Stream<Visitable> visitableStream) {
		return visitableStream.noneMatch(visitable -> visitable.accept(this) != Boolean.TRUE);
	}

	static boolean checkVariableName(final String elVarName) {
		if (EL_VARNAME_PATTERN.matcher(elVarName).matches()) {
			return true;
		}
		logWarn("'{}' is not a valid EL variable name", elVarName);
		return false;
	}

	private static void logWarn(String message, Object... args) {
		String logMessage = "Error in rule configuration: " + message;
		logger.warn(logMessage, args);
	}

	private static void logWarn(String message, Exception e) {
		String logMessage = "Error in rule configuration: " + message;
		logger.warn(logMessage, e);
	}
}
