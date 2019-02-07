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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.todo.api.ToDoAbortInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoCloseInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoFindRequest;
import com.camline.projects.smardes.todo.api.ToDoGetDetailsRequest;
import com.camline.projects.smardes.todo.api.ToDoGetInstanceDetailsRequest;
import com.camline.projects.smardes.todo.api.ToDoGetRunningInstancesRequest;
import com.camline.projects.smardes.todo.api.ToDoMarkStepClosedRequest;
import com.camline.projects.smardes.todo.api.ToDoMarkStepOpenRequest;
import com.camline.projects.smardes.todo.api.ToDoStartInstanceRequest;

public class ToDoClients extends AbstractClients {
	public ToDoClients() throws NamingException, IOException {
		super("ToDo", true);
	}

	@Override
	protected boolean mainMenu() throws IOException, JMSException {
		System.out.println("\nToDo Clients");
		System.out.println("---------------------------------");
		System.out.println("1 - ToDoFind");
		System.out.println("2 - ToDoGetDetails");
		System.out.println("3 - ToDoStartInstance");
		System.out.println("4 - ToDoGetRunningInstances");
		System.out.println("5 - ToDoMarkStepClosed");
		System.out.println("6 - ToDoMarkStepOpen");
		System.out.println("7 - ToDoGetInstanceDetails");
		System.out.println("8 - ToDoCloseInstance");
		System.out.println("9 - ToDoAbortInstance");
		System.out.println("X - Exit");

		final String command = StringUtils.defaultString(in.readString("Command"));

		switch (command.toLowerCase()) {
		case "1":
			doToDoFind();
			return true;
		case "2":
			doToDoGetDetails();
			return true;
		case "3":
			doToDoStartInstance();
			return true;
		case "4":
			doToDoGetRunningInstances();
			return true;
		case "5":
			doToDoMarkStepClosed();
			return true;
		case "6":
			doToDoMarkStepOpen();
			return true;
		case "7":
			doToDoGetInstanceDetails();
			return true;
		case "8":
			doToDoCloseInstance();
			return true;
		case "9":
			doToDoAbortInstance();
			return true;
		case "x":
			return false;
		default:
			return true;
		}
	}

	private void doToDoFind() throws JMSException, IOException {
		final String domain = in.readString("Domain");

		Map<String, String> context = new HashMap<>();
		for (;;) {
			String name = in.readString("Name");
			if (name.isEmpty()) {
				break;
			}
			String value = in.readString("Value");
			context.put(name, value);
		}

		final ToDoFindRequest request = new ToDoFindRequest(domain, context);
		sendReceiveMessage(request);
	}

	private void doToDoGetDetails() throws IOException, JMSException {
		final String domain = in.readString("Domain");
		final String todolist = in.readString("ToDoList");

		final ToDoGetDetailsRequest request = new ToDoGetDetailsRequest(domain, todolist);
		sendReceiveMessage(request);
	}

	private void doToDoStartInstance() throws JMSException, IOException {
		final String domain = in.readString("Domain");
		final String todolist = in.readString("ToDoList");
		final String startedBy = in.readString("Started By");

		Map<String, String> context = new HashMap<>();
		for (;;) {
			String name = in.readString("Name");
			if (name.isEmpty()) {
				break;
			}
			String value = in.readString("Value");
			context.put(name, value);
		}

		final ToDoStartInstanceRequest request = new ToDoStartInstanceRequest(domain, todolist, startedBy, context);
		sendReceiveMessage(request);
	}

	private void doToDoGetRunningInstances() throws JMSException, IOException {
		final String domain = in.readString("Domain");

		Map<String, String> context = new HashMap<>();
		for (;;) {
			String name = in.readString("Name");
			if (name.isEmpty()) {
				break;
			}
			String value = in.readString("Value");
			context.put(name, value);
		}

		final ToDoGetRunningInstancesRequest request = new ToDoGetRunningInstancesRequest(domain, context);
		sendReceiveMessage(request);
	}

	private void doToDoMarkStepClosed() throws IOException, JMSException {
		final UUID instanceId = in.readUUID("Instance Id");
		final int step = in.readInt("Step");
		final String closedBy = in.readString("Closed By");

		final ToDoMarkStepClosedRequest request = new ToDoMarkStepClosedRequest(instanceId, step, closedBy);
		sendReceiveMessage(request);
	}

	private void doToDoMarkStepOpen() throws IOException, JMSException {
		final UUID stepId = in.readUUID("Step Id");

		final ToDoMarkStepOpenRequest request = new ToDoMarkStepOpenRequest(stepId);
		sendReceiveMessage(request);
	}

	private void doToDoGetInstanceDetails() throws IOException, JMSException {
		final UUID instanceId = in.readUUID("Instance Id");

		final ToDoGetInstanceDetailsRequest request = new ToDoGetInstanceDetailsRequest(instanceId);
		sendReceiveMessage(request);
	}

	private void doToDoCloseInstance() throws IOException, JMSException {
		final UUID instanceId = in.readUUID("Instance Id");
		final String closedBy = in.readString("Closed By");
		final boolean force = in.readBoolean("Force (ignore open steps)");

		final ToDoCloseInstanceRequest request = new ToDoCloseInstanceRequest(instanceId, closedBy, Boolean.valueOf(force));
		sendReceiveMessage(request);
	}

	private void doToDoAbortInstance() throws IOException, JMSException {
		final UUID instanceId = in.readUUID("Instance Id");
		final String abortedBy = in.readString("Aborted By");

		final ToDoAbortInstanceRequest request = new ToDoAbortInstanceRequest(instanceId, abortedBy);
		sendReceiveMessage(request);
	}

	public static void main(final String[] args) throws NamingException, IOException {
		final ToDoClients client = new ToDoClients();
		client.execute();

		System.out.println("Exiting...");
	}
}
