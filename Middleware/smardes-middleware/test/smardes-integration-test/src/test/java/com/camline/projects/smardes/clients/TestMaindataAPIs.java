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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.camline.projects.smardes.common.el.ExpressionEngine;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.maindata.api.MainDataFileRequest;
import com.camline.projects.smardes.maindata.api.MainDataQueryRequest;

public class TestMaindataAPIs {
	private static JMSClientEx jmsClientMaindata;
	private static JMSClientEx jmsClientResources;

	@BeforeClass
	public static void init() throws NamingException, IOException {
		jmsClientMaindata = new JMSClientEx("MainData", true);
		jmsClientResources = new JMSClientEx("Resources", true);
	}

	@Test
	public void testNonExistingFile() throws JMSException {
		final MainDataFileRequest request = new MainDataFileRequest(Arrays.asList("devices"), "Device2.json");
		ELMessage elMessage =  jmsClientMaindata.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elMessage.getBody());
		assertEquals("maindata", expressionEngine.evaluateExpression("body.error.module"));
		assertEquals("IO_ERROR", expressionEngine.evaluateExpression("body.error.errorCode"));
	}

	@Test
	public void testFileRequest() throws JMSException {
		final MainDataFileRequest request = new MainDataFileRequest(Arrays.asList("devices"), "Device1.json");
		ELMessage elResponse =  jmsClientMaindata.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elResponse.getBody());
		assertEquals("Device1", expressionEngine.evaluateExpression("body.id"));
	}

	@Test
	public void testDataSelectUnknownQuery() throws JMSException {
		final MainDataQueryRequest request = new MainDataQueryRequest("maindata.notexist", null, null);
		ELMessage elResponse =  jmsClientMaindata.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elResponse.getBody());
		assertEquals("dataquery", expressionEngine.evaluateExpression("body.error.module"));
		assertEquals("QUERY_NOT_FOUND", expressionEngine.evaluateExpression("body.error.errorCode"));
	}

	@Test
	public void testDataSelectAll() throws JMSException {
		final MainDataQueryRequest request = new MainDataQueryRequest("maindata.benutzergruppen1", null, null);
		ELMessage elResponse =  jmsClientMaindata.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elResponse.getBody());
		List<Map<String, Object>> result = expressionEngine.evaluateExpression("body.responseCollection");
		assertEquals("Sheet Benutzergruppen contains 8 entries", 8, result.size());

		List<Map<String, Object>> subResult = result.stream().filter(map -> "PM1".equals(map.get("MITARBEITER"))).collect(Collectors.toList());
		assertEquals("There is only one MITARBEITER Hanisch", 1, subResult.size());
		Map<String, Object> row = subResult.get(0);

		assertEquals(2, row.size());
		assertEquals("Produktionsmitarbeiter 5", row.get("GRUPPE"));
	}

	@Test
	public void testDataSelectNamedBind() throws JMSException {
		Map<String, Object> bindParameter = new HashMap<>();
		bindParameter.put("gruppe", "Logistik 5");
		final MainDataQueryRequest request = new MainDataQueryRequest("maindata.benutzergruppen2", bindParameter, null);
		ELMessage elResponse =  jmsClientMaindata.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elResponse.getBody());
		List<Map<String, Object>> result = expressionEngine.evaluateExpression("body.responseCollection");
		assertEquals("Sheet Benutzergruppen contains 2 entries for GRUPPE 'Logistik 5'", 2, result.size());

		List<Map<String, Object>> subResult = result.stream().filter(map -> "LG2".equals(map.get("MITARBEITER"))).collect(Collectors.toList());
		assertEquals("There is only one MITARBEITER LG2", 1, subResult.size());
		Map<String, Object> row = subResult.get(0);

		assertEquals(3, row.size());
		assertEquals(Integer.valueOf(1), row.get("LEVEL"));
	}

	@Test
	public void testDataSelectPosBind() throws JMSException {
		Map<String, Object> bindParameter = new HashMap<>();
		bindParameter.put("gruppe", "Logistik 5");
		final MainDataQueryRequest request = new MainDataQueryRequest("maindata.benutzergruppen3", null, Arrays.asList(Integer.valueOf(1)));
		ELMessage elResponse =  jmsClientMaindata.sendReceiveMessage(request);

		ExpressionEngine expressionEngine = new ExpressionEngine();
		expressionEngine.setContextVariable("body", elResponse.getBody());
		List<Map<String, Object>> result = expressionEngine.evaluateExpression("body.responseCollection");
		assertEquals("Sheet Benutzergruppen contains 2 entries for LEVEL > 1", 2, result.size());

		List<Map<String, Object>> subResult = result.stream().filter(map -> "LG1".equals(map.get("MITARBEITER"))).collect(Collectors.toList());
		assertEquals("There is only one MITARBEITER Vogler", subResult.size(), 1);
		Map<String, Object> row = subResult.get(0);

		assertEquals(3, row.size());
		assertEquals(Integer.valueOf(2), row.get("LEVEL"));
		assertEquals("Logistik 5", row.get("GRUPPE"));
	}

	@AfterClass
	public static void shutdown() {
		if (jmsClientMaindata != null) {
			jmsClientMaindata.close();
		}
		if (jmsClientResources != null) {
			jmsClientResources.close();
		}
	}
}
