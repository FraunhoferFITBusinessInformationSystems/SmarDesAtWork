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

import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.jsonapi.jms.JSONServiceHandler;
import com.camline.projects.smardes.rule.api.Summary;
import com.camline.projects.smartdev.ruledef.Rules.RuleGroup;
import com.camline.projects.smartdev.ruledef.Visitable;

class RuleGroupHandler extends JSONServiceHandler {
	private static final Logger logger = LoggerFactory.getLogger(RuleGroupHandler.class);

	private final RuleEngineContext ruleEngineContext;
	private final RuleGroup ruleGroup;

	RuleGroupHandler(final RuleEngineContext ruleEngineContext, final RuleGroup ruleGroup) {
		super(ruleEngineContext.getJmsContextService());
		this.ruleEngineContext = ruleEngineContext;
		this.ruleGroup = ruleGroup;
	}

	void createConsumer() {
		setupConsumer(ruleGroup.getSelector().getAddress(), ruleGroup.getSelector().getCriteria());
	}

	@Override
	protected String getErrorModule() {
		return "rule";
	}

	@Override
	protected BodyType getBodyType(final Message message) {
		return ruleGroup.getSelector().isJsonBody() ? BodyType.JSON : BodyType.TEXT;
	}

	@Override
	protected Object doMessage(final Message message, final String bodyText, final boolean replyExpected) {
		long begin = System.currentTimeMillis();
		logger.info("Got message for rule group '{}'", ruleGroup.getName());

		final ELMessage elMessage = new ELMessage(message, bodyText, ruleGroup.getSelector().isJsonBody());

		final ExpressionHandler expressionHandler = new ExpressionHandler();
		expressionHandler.setupMessageContext(ruleEngineContext, elMessage, ruleGroup.getSelector().getAddress());
		JSONB.instance().dumpObject("EL Context Variables", expressionHandler.getContextVariables());

		RuleGroupContext ruleGroupContext = new RuleGroupContext(ruleEngineContext, expressionHandler, elMessage);
		RuleGroupVisitor ruleGroupVisitor = new RuleGroupVisitor(ruleGroupContext);

		final List<String> unhandled = ruleGroup.getRuleOrRuleSet().stream().map(Visitable.class::cast)
				.filter(ruleOrRuleSet -> ruleOrRuleSet.accept(ruleGroupVisitor) != Boolean.TRUE)
				.map(action -> action.getClass().getSimpleName()).collect(Collectors.toList());

		if (!unhandled.isEmpty()) {
			logger.warn("Unhandled or failed rule group members {}", unhandled);
		}

		if (!ruleGroupVisitor.isHandled()) {
			logger.warn("Message is not handled by any rule!");
		}

		Summary summary = ruleGroupContext.getSummary();
		long duration = System.currentTimeMillis() - begin;
		summary.setDuration(duration);
		logger.info("{} ms to execute the following rules and actions: {}", Long.valueOf(duration), summary);

		return replyExpected ? summary : null;
	}
}
