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
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;

import com.camline.projects.smardes.common.jms.JMSClient;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.jsonapi.jms.CommonErrors;

public final class JMSClientEx extends JMSClient {
	public JMSClientEx(String topicName, boolean withReply) throws NamingException, IOException {
		super(topicName, withReply);
	}

	public JMSClientEx(JMSContext jmsContext, String topicName, boolean withReply) {
		super(jmsContext, topicName, withReply);
	}

	public void sendMessage(final Object body) throws JMSException {
		sendMessage(body.getClass().getSimpleName(), JSONB.instance().marshal(body));
	}

	public void sendMessage(final Object body, final Map<String, String> properties) throws JMSException {
		sendMessage(body.getClass().getSimpleName(), JSONB.instance().marshal(body), properties);
	}

	public static String stripRequestSuffix(Class<?> requestClass) {
		String name = requestClass.getSimpleName();
		if (name.endsWith("Request")) {
			return name.substring(0, name.length() - 7);
		}
		return name;
	}

	public ELMessage sendReceiveMessage(final Object body) throws JMSException {
		final Message response = sendReceiveMessage(stripRequestSuffix(body.getClass()), JSONB.instance().marshal(body));
		if (response != null) {
			return new ELMessage(response, true);
		}

		String json = JSONB.instance().marshalToString(new ErrorResponse("Test", CommonErrors.TIMEOUT, "No message within timeout", null));
		return new ELMessage(null, json, true);
	}

	public ELMessage sendNONJSONMessage(String subject, String message) throws JMSException {
		final Message response = sendReceiveMessage(subject, message.getBytes(StandardCharsets.UTF_8));
		if (response != null) {
			return new ELMessage(response, true);
		}

		String json = JSONB.instance().marshalToString(new ErrorResponse("Test", CommonErrors.TIMEOUT, "No message within timeout", null));
		return new ELMessage(null, json, true);
	}

	public ELMessage sendEmptyMessage(String subject) throws JMSException {
		final Message response = sendReceiveMessage(subject, null);
		if (response != null) {
			return new ELMessage(response, true);
		}

		String json = JSONB.instance().marshalToString(new ErrorResponse("Test", CommonErrors.TIMEOUT, "No message within timeout", null));
		return new ELMessage(null, json, true);
	}
}
