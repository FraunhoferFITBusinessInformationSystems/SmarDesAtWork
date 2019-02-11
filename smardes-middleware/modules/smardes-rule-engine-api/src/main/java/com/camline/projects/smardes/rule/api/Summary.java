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
package com.camline.projects.smardes.rule.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

public class Summary {
	private long duration;
	private int evaluatedConditions;
	private List<ExecutedRule> executedRules;

	public Summary() {
		this.executedRules = new ArrayList<>();
	}

	public void addRuleSummary(final String rule, final long duration_,
			final Collection<Pair<String, Exception>> actions) {
		executedRules.add(new ExecutedRule(rule, duration_, actions));
	}

	public void setExecutedRules(List<ExecutedRule> executedRules) {
		this.executedRules = executedRules;
	}

	public List<ExecutedRule> getExecutedRules() {
		return executedRules;
	}

	public void conditionEvaluated() {
		evaluatedConditions++;
	}

	public int getEvaluatedConditions() {
		return evaluatedConditions;
	}

	public void setEvaluatedConditions(int evaluatedConditions) {
		this.evaluatedConditions = evaluatedConditions;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return executedRules.stream()
				.map(executedRule -> executedRule.getRule() + " [" + executedRule.getDuration() + " ms]" + ": "
						+ executedRule.getActions().stream()
								.map(action -> action.getActionName()
										+ (action.getErrorMessage() != null ? ("->" + action.getErrorMessage()) : ""))
								.collect(Collectors.joining(", ")))
				.collect(Collectors.joining("\n\t", "\n\t", ""))
				+ "\n\t" + evaluatedConditions + " conditions evaluated.";
	}
}
