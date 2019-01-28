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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.camline.projects.smardes.todo.api.Errors;
import com.camline.projects.smardes.todo.api.ToDoAbortInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoCloseInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoFindRequest;
import com.camline.projects.smardes.todo.api.ToDoGetDetailsRequest;
import com.camline.projects.smardes.todo.api.ToDoGetInstanceDetailsRequest;
import com.camline.projects.smardes.todo.api.ToDoGetRunningInstancesRequest;
import com.camline.projects.smardes.todo.api.ToDoMarkStepClosedRequest;
import com.camline.projects.smardes.todo.api.ToDoStartInstanceRequest;

public class TestTODOAPIs {
	private static APIInvoker apiInvoker;

	@BeforeClass
	public static void init() throws NamingException, IOException {
		apiInvoker = new APIInvoker("ToDo");
	}

	@Test
	public void testNonExisting() throws JMSException {
		List<Map<String, Object>> headers = requestHeaders("1", "102", "NotExist");
		assertTrue(headers.isEmpty());
	}

	@Test
	public void testTwoMatches() throws JMSException {
		List<Map<String, Object>> headers = requestHeaders("1", "102", "Delonghi Primadonna");
		assertEquals(2, headers.size());
		assertEquals("Primadonna1", headers.get(0).get("id"));
		assertEquals("Primadonna2", headers.get(1).get("id"));
		System.out.println(headers);
	}

	@Test
	public void testSteps() throws JMSException {
		final ToDoGetDetailsRequest request = new ToDoGetDetailsRequest("KAFFEE", "Primadonna1");
		apiInvoker.invokeRequest(request, "response");
		assertEquals("Primadonna1", apiInvoker.eval("header.id"));
		assertEquals(4, (apiInvoker.eval("steps", List.class)).size());
		assertEquals(10, apiInvoker.eval("steps[0].step", Integer.class).intValue());
		assertTrue(apiInvoker.eval("steps[3].description", String.class).contains("Schrauben"));
	}

	@Test
	public void testStartInstance() throws JMSException {
		startInstance(null);
		assertEquals(Collections.emptyMap(), apiInvoker.eval("instance.context"));

		startInstance("4711");
		assertEquals("4711", apiInvoker.eval("instance.context['Auftrag']"));
	}

	@Test
	public void testCloseInstance() throws JMSException {
		UUID instanceId = startInstance(null);
		List<Map<String, Object>> steps = apiInvoker.eval("steps");

		boolean success = closeInstance(instanceId, null);
		assertFalse(success);
		assertEquals(Errors.TODO_LIST_NOT_COMPLETED.name(), apiInvoker.evalError("errorCode"));

		success = closeInstance(instanceId, Boolean.FALSE);
		assertFalse(success);
		assertEquals(Errors.TODO_LIST_NOT_COMPLETED.name(), apiInvoker.evalError("errorCode"));

		for (Map<String, Object> step : steps) {
			Integer stepNo = (Integer) step.get("step");
			success = markStepClosed(instanceId, stepNo.intValue(), false);
			assertTrue(success);
		}

		success = closeInstance(instanceId, Boolean.FALSE);
		assertTrue(success);

		success = closeInstance(instanceId, Boolean.FALSE);
		assertFalse(success);
		assertEquals(Errors.TODO_LIST_ALREADY_CLOSED.name(), apiInvoker.evalError("errorCode"));

		success = closeInstance(instanceId, Boolean.TRUE);
		assertFalse(success);
		assertEquals(Errors.TODO_LIST_ALREADY_CLOSED.name(), apiInvoker.evalError("errorCode"));
	}

	@Test
	public void testCloseInstanceForce() throws JMSException {
		UUID instanceId = startInstance(null);
		boolean success = closeInstance(instanceId, Boolean.TRUE);
		assertTrue(success);
	}

	@Test
	public void testAbortInstance() throws JMSException {
		UUID instanceId = startInstance(null);
		boolean success = abortInstance(instanceId);
		assertTrue(success);
		assertEquals("test_abort", apiInvoker.eval("instance.abortedBy"));
		success = abortInstance(instanceId);
		assertFalse(success);
		assertEquals(Errors.TODO_LIST_ALREADY_ABORTED.name(), apiInvoker.evalError("errorCode"));

		ToDoGetRunningInstancesRequest request = new ToDoGetRunningInstancesRequest("KAFFEE",
				createContext("1", "102", "Delonghi Primadonna"));
		apiInvoker.invokeRequest(request, "response3");
		List<?> instanceIds = apiInvoker.evalRaw(
				"el:select(response3.responseObject.instances, 'instance', 'instance.id')");
		assertFalse(instanceIds.contains(instanceId.toString()));
	}

	private UUID startInstance(String auftrag) throws JMSException {
		List<Map<String, Object>> headers = requestHeaders("1", "102", "Delonghi Primadonna");
		Map<String, String> context = new HashMap<>();
		if (auftrag != null) {
			context.put("Auftrag", auftrag);
		}

		final ToDoStartInstanceRequest request1 = new ToDoStartInstanceRequest("KAFFEE",
				headers.get(0).get("id").toString(), "test", context);
		apiInvoker.invokeRequest(request1, "response1");
		UUID instanceId = UUID.fromString(apiInvoker.eval("instanceId"));

		final ToDoGetInstanceDetailsRequest request2 = new ToDoGetInstanceDetailsRequest(instanceId);
		apiInvoker.invokeRequest(request2, "response2");

		return instanceId;
	}

	private boolean markStepClosed(UUID instanceId, int stepNo, boolean details) throws JMSException {
		ToDoMarkStepClosedRequest request1 = new ToDoMarkStepClosedRequest(instanceId, stepNo, "test_close_" + stepNo);
		boolean success = apiInvoker.invokeRequest(request1, "response1");

		if (details && success) {
			final ToDoGetInstanceDetailsRequest request2 = new ToDoGetInstanceDetailsRequest(instanceId);
			apiInvoker.invokeRequest(request2, "response2");
		}

		return success;
	}

	private boolean closeInstance(UUID instanceId, Boolean force) throws JMSException {
		ToDoCloseInstanceRequest request1 = new ToDoCloseInstanceRequest(instanceId, "test_close", force);
		boolean success = apiInvoker.invokeRequest(request1, "response1");

		if (success) {
			final ToDoGetInstanceDetailsRequest request2 = new ToDoGetInstanceDetailsRequest(instanceId);
			apiInvoker.invokeRequest(request2, "response2");
		}

		return success;
	}

	private boolean abortInstance(UUID instanceId) throws JMSException {
		ToDoAbortInstanceRequest request1 = new ToDoAbortInstanceRequest(instanceId, "test_abort");
		boolean success = apiInvoker.invokeRequest(request1, "response1");

		if (success) {
			final ToDoGetInstanceDetailsRequest request2 = new ToDoGetInstanceDetailsRequest(instanceId);
			apiInvoker.invokeRequest(request2, "response2");
		}

		return success;
	}

	private static Map<String, String> createContext(String stockwerk, String raum, String kaffeemaschine) {
		Map<String, String> context = new HashMap<>();
		context.put("STOCKWERK", stockwerk);
		context.put("RAUM", raum);
		context.put("KAFFEEMASCHINE", kaffeemaschine);
		return context;
	}

	private static List<Map<String, Object>> requestHeaders(String stockwerk, String raum, String kaffeemaschine)
			throws JMSException {
		final ToDoFindRequest request = new ToDoFindRequest("KAFFEE", createContext(stockwerk, raum, kaffeemaschine));

		apiInvoker.invokeRequest(request, "header");

		return apiInvoker.eval("headers");
	}

	@AfterClass
	public static void shutdown() {
		if (apiInvoker != null) {
			apiInvoker.close();
		}
	}
}
