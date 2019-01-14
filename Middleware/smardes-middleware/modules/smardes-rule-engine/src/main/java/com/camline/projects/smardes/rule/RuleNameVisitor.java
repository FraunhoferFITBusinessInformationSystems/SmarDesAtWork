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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.camline.projects.smartdev.ruledef.BaseVisitor;
import com.camline.projects.smartdev.ruledef.RuleSetType;
import com.camline.projects.smartdev.ruledef.RuleType;
import com.camline.projects.smartdev.ruledef.Rules;
import com.camline.projects.smartdev.ruledef.SetELContextType;
import com.camline.projects.smartdev.ruledef.Visitable;
import com.camline.projects.smartdev.ruledef.Rules.GlobalVariables;
import com.camline.projects.smartdev.ruledef.Rules.RuleGroup;
import com.camline.projects.smartdev.ruledef.Rules.PatchMessageBodyPrototypes.PatchMessageBodyPrototype;

class RuleNameVisitor extends BaseVisitor<Object, RuntimeException> {
	private final Map<String, Object> globalVariables = new HashMap<>();
	private Map<String, SetELContextType> patchMessageBodyPrototypes = Collections.emptyMap();
	private final Map<String, RuleType> rules = new HashMap<>();

	@Override
	public Object visit(Rules ruleDefinition) {
		if (ruleDefinition.getPatchMessageBodyPrototypes() != null) {
			patchMessageBodyPrototypes = ruleDefinition.getPatchMessageBodyPrototypes()
					.getPatchMessageBodyPrototype().stream()
					.collect(Collectors.toMap(PatchMessageBodyPrototype::getName, Function.identity()));
		}

		final ExpressionHandler expressionHandler = new ExpressionHandler();
		expressionHandler.setContextVariable(ExpressionHandler.GLOBAL_VARIABLES, globalVariables);
		ruleDefinition.getGlobalVariables().getProperty()
				.forEach(property -> expressionHandler.patchValue(ExpressionHandler.GLOBAL_VARIABLES, property));

		doVisit(ruleDefinition.getRuleGroup());

		return null;
	}


	@Override
	public Object visit(GlobalVariables globalVariablesDefinition) {
		final ExpressionHandler expressionHandler = new ExpressionHandler();

		expressionHandler.setContextVariable(ExpressionHandler.GLOBAL_VARIABLES, globalVariables);

		globalVariablesDefinition.getProperty()
				.forEach(property -> expressionHandler.patchValue(ExpressionHandler.GLOBAL_VARIABLES, property));
		return null;
	}

	@Override
	public Object visit(RuleGroup ruleGroup) {
		doVisit(ruleGroup.getRuleOrRuleSet());
		return null;
	}

	@Override
	public Object visit(RuleSetType ruleSet) {
		doVisit(ruleSet.getRuleOrRuleSet());
		return null;
	}

	@Override
	public Object visit(RuleType rule) {
		rules.put(rule.getName(), rule);
		return null;
	}

	@SafeVarargs
	private final void doVisit(List<? extends Visitable>... visitableLists) {
		doVisit(Stream.of(visitableLists).flatMap(List::stream));
	}

	private void doVisit(Stream<Visitable> visitableStream) {
		visitableStream.forEach(visitable -> visitable.accept(this));
	}

	private void doVisit(List<Object> choiceElements) {
		doVisit(choiceElements.stream().map(Visitable.class::cast));
	}

	public Map<String, Object> getGlobalVariables() {
		return globalVariables;
	}

	public Map<String, SetELContextType> getPatchMessageBodyPrototypes() {
		return patchMessageBodyPrototypes;
	}

	public Map<String, RuleType> getRules() {
		return rules;
	}
}
