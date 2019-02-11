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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.camline.projects.smardes.common.el.ExpressionEngine;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.resources.api.GetResourceRequest;
import com.camline.projects.smardes.resources.api.PutResourceRequest;

public class TestResourceAPIs {
	private static JMSClientEx jmsClient;

	@BeforeClass
	public static void init() throws NamingException, IOException {
		jmsClient = new JMSClientEx("Resources", true);
	}

	@Test
	public void testNonExisting() throws JMSException {
		final GetResourceRequest request = new GetResourceRequest("00000000-0000-0000-0000-000000000000");
		ELMessage elMessage =  jmsClient.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elMessage.getBody());
		assertEquals("UNKNOWN_RESOURCE", expressionEngine.evaluateExpression("body.error.errorCode"));
	}

	@Test
	public void testPutResource() throws JMSException, IOException {
		String pathName = "properties/log4j.properties";
		String mimeType = "plain/text";

		final Path path = Paths.get(pathName);
		byte[] body = Files.readAllBytes(path);

		ELMessage putResponse = jmsClient.sendReceiveMessage(new PutResourceRequest(path.getFileName().toString(), mimeType, body));

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body1", putResponse.getBody());
		String uuid = expressionEngine.evaluateExpression("body1.responseObject.uuid");

		ELMessage getResponse =  jmsClient.sendReceiveMessage(new GetResourceRequest(uuid));

		expressionEngine.setContextVariable("body2", getResponse.getBody());
		assertEquals(uuid, expressionEngine.evaluateExpression("body2.responseObject.uuid"));
		assertEquals(path.getFileName().toString(), expressionEngine.evaluateExpression("body2.responseObject.name"));
		assertEquals(mimeType, expressionEngine.evaluateExpression("body2.responseObject.mimeType"));

		String base64 = expressionEngine.evaluateExpression("body2.responseObject.body");
		byte[] body2 = Base64.getDecoder().decode(base64);
		assertArrayEquals(body, body2);
	}

	@AfterClass
	public static void shutdown() {
		if (jmsClient != null) {
			jmsClient.close();
		}
	}
}
