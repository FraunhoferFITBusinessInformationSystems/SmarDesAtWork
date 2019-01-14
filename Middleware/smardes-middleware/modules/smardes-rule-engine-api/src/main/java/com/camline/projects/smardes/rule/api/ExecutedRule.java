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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;

import org.apache.commons.lang3.tuple.Pair;

@JsonbPropertyOrder({"rule", "actions"})
public class ExecutedRule {
	private String rule;
	private long duration;
	private List<ActionResult> actions;

	@JsonbCreator
	public ExecutedRule(@JsonbProperty("rule") String rule, @JsonbProperty("duration") long duration,
			@JsonbProperty("actions") List<ActionResult> actions) {
		this.rule = rule;
		this.duration = duration;
		this.actions = actions;
	}

	public ExecutedRule(final String rule, final long duration, final Collection<Pair<String, Exception>> actions) {
		this.rule = rule;
		this.duration = duration;
		this.actions = actions.stream()
				.map(actionPair -> new ActionResult(actionPair.getLeft(),
						actionPair.getRight() != null ? actionPair.getRight().getMessage() : null))
				.collect(Collectors.toList());
	}

	public String getRule() {
		return rule;
	}

	public long getDuration() {
		return duration;
	}

	public List<ActionResult> getActions() {
		return actions;
	}
}
