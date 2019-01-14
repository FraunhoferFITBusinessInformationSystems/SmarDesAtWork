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

import static com.camline.projects.smardes.clients.ELAssert.assertExprEquals;
import static com.camline.projects.smardes.clients.ELAssert.assertExprIsLocalISO8601;
import static com.camline.projects.smardes.clients.ELAssert.assertExprIsOffsetISO8601;
import static com.camline.projects.smardes.clients.ELAssert.assertExprNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.resources.api.PutResourceRequest;

public class TestDBMonitor {
	private static JMSContext jmsContext;
	private static JMSClientEx jmsClient1;
	private static JMSClientEx jmsClient2;
	private static DBMonEventHandler dbMonEventHandler;
	private static BlockingQueue<ELMessage> messageQueue;

	@BeforeClass
	public static void init() throws NamingException, IOException {
		final JMSContextFactory jmsContextFactory = new JMSContextFactory(TestDBMonitor.class.getSimpleName());
		jmsContext = jmsContextFactory.createContext();
		jmsClient1 = new JMSClientEx("Resources", true);
		jmsClient2 = new JMSClientEx("RuleEngine", false);
		dbMonEventHandler = new DBMonEventHandler(jmsContext);
		dbMonEventHandler.createConsumer("sdgw");
		messageQueue = new LinkedBlockingQueue<>();
	}

	@Test
	public void monitorResourceChanges() throws IOException, JMSException, InterruptedException {
		String pathName = "properties/artemis.properties";
		String mimeType = "plain/text";

		final Path path = Paths.get(pathName);
		String fileName = path.getFileName().toString();
		byte[] body = Files.readAllBytes(path);

		ELMessage putResponse = jmsClient1.sendReceiveMessage(new PutResourceRequest(fileName, mimeType, body));

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", putResponse.getBody());
		String uuid = expressionEngine.evaluateExpression("body.responseObject.uuid");
		assertNotNull(uuid);

		String id = null;
		for (int i = 1; i <= 5; i++) {
			ELMessage message = messageQueue.poll(i == 1 ? 20 : 1, TimeUnit.SECONDS);
			assertNotNull("Expect to get 5 messages, message " + i + " is missing", message);

			expressionEngine.setContextVariable("body", message.getBody());
			assertExprEquals("Job", expressionEngine, "body.Type");
			assertExprEquals("test", expressionEngine, "body.Source");
			assertExprEquals(fileName, expressionEngine, "body.Content.NAME");
			assertExprEquals(fileName, expressionEngine, "body.Resource.NAME");
			assertExprIsLocalISO8601(expressionEngine, "body.Resource.LASTACCESSED");
			assertExprEquals(uuid, expressionEngine, "body.Resource.UUID");
			assertExprIsOffsetISO8601(expressionEngine, "body.CreatedAt");
			assertExprIsOffsetISO8601(expressionEngine, "body.AssignedAt");
			assertExprEquals("user" + i + ".A", expressionEngine, "body.AssignedTo");
			id = expressionEngine.evaluateExpression("body.Id");
			expressionEngine.setValue("body.ReferenceId", id);
			expressionEngine.setValue("body.Id", expressionEngine.evaluateExpression("uuid:random()"));

			jmsClient2.sendMessage(message.getBody());
		}

		for (int i = 1; i <= 5; i++) {
			ELMessage statusUpdate = messageQueue.poll(1, TimeUnit.SECONDS);
			assertNotNull(statusUpdate);

			expressionEngine.setContextVariable("body", statusUpdate.getBody());
			assertExprEquals("Notification", expressionEngine, "body.Type");
			assertExprEquals("StatusUpdate", expressionEngine, "body.Name");
			assertExprEquals(id, expressionEngine, "body.Id");
			assertExprEquals(id, expressionEngine, "body.ReferenceId");
			assertExprNull(expressionEngine, "body.Content");
			assertExprIsLocalISO8601(expressionEngine, "body.Resource.LASTACCESSED");
			assertExprIsOffsetISO8601(expressionEngine, "body.CreatedAt");
			assertExprIsOffsetISO8601(expressionEngine, "body.AssignedAt");
			assertExprEquals("AssignedTo must cover all recipients", "user" + i + ".A", expressionEngine, "body.AssignedTo");
		}

		ELMessage message = messageQueue.poll(1, TimeUnit.SECONDS);
		assertNull(message);
	}

	@AfterClass
	public static void shutdown() {
		if (jmsClient1 != null) {
			jmsClient1.close();
		}
		if (jmsClient2 != null) {
			jmsClient2.close();
		}
		jmsContext.close();
	}

	public static class DBMonEventHandler extends DumpMessageHandler {
		private static final Logger logger = LoggerFactory.getLogger(TestDBMonitor.DBMonEventHandler.class);

		public DBMonEventHandler(JMSContext jmsContext) {
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
