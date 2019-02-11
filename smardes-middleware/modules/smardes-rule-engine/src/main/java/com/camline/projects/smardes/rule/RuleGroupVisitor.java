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

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smartdev.ruledef.BaseVisitor;
import com.camline.projects.smartdev.ruledef.RuleSetType;
import com.camline.projects.smartdev.ruledef.RuleType;
import com.camline.projects.smartdev.ruledef.Visitable;

class RuleGroupVisitor extends BaseVisitor<Boolean, RuntimeException> {
	private static final Logger logger = LoggerFactory.getLogger(RuleGroupVisitor.class);

	private final RuleGroupContext ruleGroupContext;

	private boolean handled;

	RuleGroupVisitor(RuleGroupContext ruleGroupContext) {
		this.ruleGroupContext = ruleGroupContext;
	}

	@Override
	public Boolean visit(RuleSetType ruleSet) {
		if (!ruleGroupContext.evaluateCondition(ruleSet.getCondition())) {
			logger.debug("Skip rule set '{}'", ruleSet.getName());
			return Boolean.TRUE;
		}

		final List<String> unhandled = ruleSet.getRuleOrRuleSet().stream().map(Visitable.class::cast)
				.filter(ruleOrRuleSet -> ruleOrRuleSet.accept(this) != Boolean.TRUE)
				.map(action -> action.getClass().getSimpleName()).collect(Collectors.toList());

		if (!unhandled.isEmpty()) {
			logger.warn("Unhandled or failed rule group members {}", unhandled);
		}

		return Boolean.TRUE;
	}

	@Override
	public Boolean visit(RuleType rule) {
		if (!rule.isInternal()) {
			ruleGroupContext.executeRule(rule);
			handled = true;
		}

		return Boolean.TRUE;
	}

	boolean isHandled() {
		return handled;
	}
}
