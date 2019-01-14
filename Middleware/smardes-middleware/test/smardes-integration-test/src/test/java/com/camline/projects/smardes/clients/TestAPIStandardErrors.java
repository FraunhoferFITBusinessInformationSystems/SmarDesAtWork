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

import java.io.IOException;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.camline.projects.smardes.common.el.ExpressionEngine;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.maindata.api.MainDataFileRequest;
import com.camline.projects.smardes.resources.api.PutResourceRequest;

public class TestAPIStandardErrors {
	private static JMSClientEx jmsClientResources;

	@BeforeClass
	public static void init() throws NamingException, IOException {
		jmsClientResources = new JMSClientEx("Resources", true);
	}

	@Test
	public void testWrongAddress() throws JMSException {
		final MainDataFileRequest request = new MainDataFileRequest(Arrays.asList("devices"), "Device1.json");
		ELMessage elMessage =  jmsClientResources.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elMessage.getBody());
		assertEquals("resource", expressionEngine.evaluateExpression("body.error.module"));
		assertEquals("REQUEST_INVALID", expressionEngine.evaluateExpression("body.error.errorCode"));
	}

	@Test
	public void testEmptyBody() throws JMSException {
		ELMessage elMessage =  jmsClientResources.sendEmptyMessage("EmptyBodyTest");

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elMessage.getBody());
		assertEquals("resource", expressionEngine.evaluateExpression("body.error.module"));
		assertEquals("NO_REQUEST_BODY", expressionEngine.evaluateExpression("body.error.errorCode"));
	}

	@Test
	public void testNoJSONBody() throws JMSException {
		ELMessage elMessage =  jmsClientResources.sendNONJSONMessage(JMSClientEx.stripRequestSuffix(PutResourceRequest.class), "abc");

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elMessage.getBody());
		assertEquals("resource", expressionEngine.evaluateExpression("body.error.module"));
		assertEquals("UNKNOWN", expressionEngine.evaluateExpression("body.error.errorCode"));
	}

	@AfterClass
	public static void shutdown() {
		if (jmsClientResources != null) {
			jmsClientResources.close();
		}
	}
}
