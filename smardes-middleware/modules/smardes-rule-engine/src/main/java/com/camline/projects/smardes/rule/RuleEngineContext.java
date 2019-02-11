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

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.jms.JMSContext;

import com.camline.projects.smartdev.ruledef.RuleType;
import com.camline.projects.smartdev.ruledef.Rules;
import com.camline.projects.smartdev.ruledef.SetELContextType;

public class RuleEngineContext {
	private final ConversationContextRepository conversationContextRepository;
	private final JMSContext jmsContextService;
	private final JMSContext jmsContextOutgoing;
	private final ScheduledExecutorService conversationMonitor;
	private final ExecutorService singletonWorker;
	private final Map<String, Object> globalVariables;
	private final Map<String, SetELContextType> patchMessageBodyPrototypes;
	private final Map<String, RuleType> rules;

	public RuleEngineContext(final ConversationContextRepository conversationContextRepository,
			final JMSContext jmsContextService, final JMSContext jmsContextOutgoing,
			final ScheduledExecutorService conversationMonitor, final ExecutorService singletonWorker,
			final Rules ruleDefinition) {
		this.conversationContextRepository = conversationContextRepository;
		this.jmsContextService = jmsContextService;
		this.jmsContextOutgoing = jmsContextOutgoing;
		this.conversationMonitor = conversationMonitor;
		this.singletonWorker = singletonWorker;

		RuleNameVisitor ruleVisitor = new RuleNameVisitor();
		ruleDefinition.accept(ruleVisitor);

		this.globalVariables = ruleVisitor.getGlobalVariables();
		this.patchMessageBodyPrototypes = ruleVisitor.getPatchMessageBodyPrototypes();
		this.rules = ruleVisitor.getRules();
	}

	public ConversationContextRepository getConversationContextRepository() {
		return conversationContextRepository;
	}

	public JMSContext getJmsContextService() {
		return jmsContextService;
	}

	public JMSContext getJmsContextOutgoing() {
		return jmsContextOutgoing;
	}

	public ScheduledExecutorService getConversationMonitor() {
		return conversationMonitor;
	}

	public ExecutorService getSingletonWorker() {
		return singletonWorker;
	}

	public Map<String, Object> getGlobalVariables() {
		return globalVariables;
	}

	public SetELContextType getPatchMessageBodyPrototype(final String name) {
		return patchMessageBodyPrototypes.get(name);
	}

	public RuleType getRule(String name) {
		return rules.get(name);
	}
}
