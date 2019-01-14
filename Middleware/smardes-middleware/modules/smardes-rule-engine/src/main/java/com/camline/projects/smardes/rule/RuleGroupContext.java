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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.jsonapi.SmarDesException;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.rule.api.Summary;
import com.camline.projects.smartdev.ruledef.ExecuteRuleType;
import com.camline.projects.smartdev.ruledef.RuleType;
import com.camline.projects.smartdev.ruledef.RuleType.Condition;
import com.camline.projects.smartdev.ruledef.Visitable;

public class RuleGroupContext {
	private static final Logger logger = LoggerFactory.getLogger(RuleGroupContext.class);

	private final RuleEngineContext ruleEngineContext;
	private final ExpressionHandler expressionHandler;
	private final ELMessage elMessage;
	private final Summary summary;

	public RuleGroupContext(RuleEngineContext ruleEngineContext, ExpressionHandler expressionHandler,
			ELMessage elMessage) {
		this.ruleEngineContext = ruleEngineContext;
		this.expressionHandler = expressionHandler;
		this.elMessage = elMessage;
		this.summary = new Summary();
	}

	public boolean evaluateCondition(String condition) {
		summary.conditionEvaluated();
		return expressionHandler.evaluateBooleanExpression(condition);
	}

	public RuleEngineContext getRuleEngineContext() {
		return ruleEngineContext;
	}

	public ExpressionHandler getExpressionHandler() {
		return expressionHandler;
	}

	public ELMessage getElMessage() {
		return elMessage;
	}

	public Summary getSummary() {
		return summary;
	}

	public void executeInternalRule(ExecuteRuleType executeRule) {
		logger.info("Executing rule '{}' internally", executeRule.getName());
		RuleType rule = ruleEngineContext.getRule(executeRule.getName());
		if (rule == null) {
			throw new SmarDesException(
					String.format("executeRule error: rule with name '%s' does not exist",
							executeRule.getName()));
		}

		List<?> iteratorVars = expressionHandler.resolveIterateOver(executeRule.getIterateOver());
		for (Object iteratorVar : iteratorVars) {
			expressionHandler.setContextVariable(executeRule.getIteratorVar(), iteratorVar);
			executeRule(rule);
		}
	}

	public void executeRule(RuleType rule) {
		List<?> iteratorVars = expressionHandler.resolveIterateOver(rule.getIterateOver());
		for (Object iteratorVar : iteratorVars) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			String ruleName = rule.getName();
			if (iteratorVar != null) {
				 ruleName += "[" + iteratorVar + "]";
			}
			expressionHandler.setContextVariable(rule.getIteratorVar(), iteratorVar);

			final Pair<Boolean, NamedConditions> result = evalConditions(rule);
			if (!result.getLeft().booleanValue()) {
				logger.debug("Skip rule '{}'", rule.getName());
				continue;
			}

			final RuleActionVisitor ruleActionVisitor = new RuleActionVisitor(this,
					rule.getName(), result.getRight());

			// Execute and return actions that were unhandled by this visitor
			final List<String> unhandledActions = rule.getActions().getPushMessageOrDumpFileOrStartConversation()
					.stream().map(Visitable.class::cast)
					.filter(action -> action.accept(ruleActionVisitor) != Boolean.TRUE)
					.map(action -> action.getClass().getSimpleName()).collect(Collectors.toList());

			stopWatch.stop();

			summary.addRuleSummary(ruleName, stopWatch.getTime(), ruleActionVisitor.getExecutedActions());

			if (!unhandledActions.isEmpty()) {
				logger.warn("Unhandled or failed rule actions {}", unhandledActions);
			}
		}

	}

	private Pair<Boolean, NamedConditions> evalConditions(final RuleType rule) {
		if (rule.getCondition().isEmpty()) {
			/*
			 * Special case: no condition is always true
			 */
			return Pair.of(Boolean.TRUE, NamedConditions.EMPTY);
		}

		try {
			if (rule.getCondition().stream().noneMatch(cond -> cond.getName() != null)) {
				/*
				 * No named conditions at all - quick
				 */
				return Pair.of(evaluateUnnamedConditions(rule), NamedConditions.EMPTY);
			}

			/*
			 * If there are named conditions - we need to evaluate all
			 */
			return evaluateNamedConditions(rule);
		} catch (final RuntimeException e) {
			logger.error("Skip rule '" + rule.getName() + "' since condition evaluation raised an exception.", e);
			return Pair.of(Boolean.FALSE, NamedConditions.EMPTY);
		}
	}

	private Boolean evaluateUnnamedConditions(final RuleType rule) {
		for (final Condition condition : rule.getCondition()) {
			if (!evaluateCondition(condition.getValue())) {
				continue;
			}
			/*
			 * Only one of the conditions must be true
			 */
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private Pair<Boolean, NamedConditions> evaluateNamedConditions(final RuleType rule) {
		final Set<String> namedConditionsFulfilled = new HashSet<>();
		final Set<String> namedConditionsNotFulfilled = new HashSet<>();
		Boolean atLeastOneConditionFulfilled = Boolean.FALSE;
		for (final Condition condition : rule.getCondition()) {
			if (!evaluateCondition(condition.getValue())) {
				if (condition.getName() != null) {
					namedConditionsNotFulfilled.add(condition.getName());
				}
				continue;
			}

			atLeastOneConditionFulfilled = Boolean.TRUE;
			if (condition.getName() != null) {
				namedConditionsFulfilled.add(condition.getName());
			}
		}

		/*
		 * Only one of the conditions must be true
		 */
		return Pair.of(atLeastOneConditionFulfilled,
				new NamedConditions(namedConditionsFulfilled, namedConditionsNotFulfilled));
	}
}
