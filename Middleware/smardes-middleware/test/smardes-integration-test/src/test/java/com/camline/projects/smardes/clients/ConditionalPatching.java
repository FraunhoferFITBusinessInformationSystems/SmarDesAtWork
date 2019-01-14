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
package com.camline.projects.smardes.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.el.ExpressionEngine;
import com.camline.projects.smardes.common.jms.DumpMessageHandler;
import com.camline.projects.smardes.common.jms.JMSContextFactory;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.rule.api.ActionResult;
import com.camline.projects.smardes.rule.api.ExecutedRule;
import com.camline.projects.smardes.rule.api.Summary;

public class ConditionalPatching {
	private static JMSContext jmsContext;
	private static JMSClientEx jmsClient;
	private static RuleEngineListener sdgwListener;
	private static BlockingQueue<ELMessage> messageQueue;

	@BeforeClass
	public static void init() throws NamingException, IOException {
		final JMSContextFactory jmsContextFactory = new JMSContextFactory(ConditionalPatching.class.getSimpleName());
		jmsContext = jmsContextFactory.createContext();
		jmsClient = new JMSClientEx("RuleEngine", true);
		sdgwListener = new RuleEngineListener(jmsContext);
		sdgwListener.createConsumer("sdgw");
		messageQueue = new LinkedBlockingQueue<>();
	}

	private static Summary unmarshalSummary(ELMessage result) {
		String jsonSummary =  JSONB.instance().marshalToString(result.getBody().get("responseObject"));
		return JSONB.instance().unmarshalBody(jsonSummary, Summary.class);
	}

	private static ExecutedRule getFirstRule(Summary summary) {
		assertFalse(summary.getExecutedRules().isEmpty());
		return summary.getExecutedRules().get(0);
	}

	private static ActionResult getFirstActionResult(ExecutedRule executedRule) {
		assertFalse(executedRule.getActions().isEmpty());
		return executedRule.getActions().get(0);
	}

	@Test
	public void case1() throws JMSException, InterruptedException {
		Map<String, String> body = new HashMap<>();
		body.put("TestName", "ConditionalCase1");

		ELMessage result = jmsClient.sendReceiveMessage(body);
		assertNotNull(result);

		Summary summary = unmarshalSummary(result);

		ExecutedRule executedRule = getFirstRule(summary);
		assertEquals("Test Queries", executedRule.getRule());
		ActionResult actionResult = getFirstActionResult(executedRule);
		assertEquals("PushMessage[1]", actionResult.getActionName());
		assertNull(actionResult.getErrorMessage());

		ELMessage message = messageQueue.poll(20, TimeUnit.SECONDS);
		assertNotNull("Expect to a message from rule \"Test Queries\"", message);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", message.getBody());
		assertEquals("ConditionalCase1", expressionEngine.evaluateExpression("body.TestName"));
		assertEquals("Case1", expressionEngine.evaluateExpression("body.Result"));
	}

	@Test
	public void case2() throws JMSException, InterruptedException {
		Map<String, String> body = new HashMap<>();
		body.put("TestName", "ConditionalCase2");

		ELMessage result = jmsClient.sendReceiveMessage(body);
		assertNotNull(result);

		Summary summary = unmarshalSummary(result);

		ExecutedRule executedRule = getFirstRule(summary);
		assertEquals("Test Queries", executedRule.getRule());
		ActionResult actionResult = getFirstActionResult(executedRule);
		assertEquals("PushMessage[1]", actionResult.getActionName());
		assertNull(actionResult.getErrorMessage());

		ELMessage message = messageQueue.poll(20, TimeUnit.SECONDS);
		assertNotNull("Expect to a message from rule \"Test Queries\"", message);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", message.getBody());
		assertEquals("ConditionalCase2", expressionEngine.evaluateExpression("body.TestName"));
		assertEquals("Case2", expressionEngine.evaluateExpression("body.Result"));
	}

	@AfterClass
	public static void shutdown() {
		if (jmsClient != null) {
			jmsClient.close();
		}
		jmsContext.close();
	}

	public static class RuleEngineListener extends DumpMessageHandler {
		private static final Logger logger = LoggerFactory.getLogger(ConditionalPatching.RuleEngineListener.class);

		public RuleEngineListener(JMSContext jmsContext) {
			super(jmsContext);
		}


		public void createConsumer(String address) {
			super.setupConsumer(address, null);
		}

		@Override
		public void onMessage(Message message) {
			super.onMessage(message);

			ELMessage elMessage = new ELMessage(message, true);
			try {
				messageQueue.put(elMessage);
			} catch (InterruptedException e) {
				logger.warn("Unexpected interrupt", e);
			}
		}
	}
}
