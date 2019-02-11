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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.jms.JMSClient;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.el.ELMessage;

public abstract class AbstractClients {
	private static final Logger logger = LoggerFactory.getLogger(AbstractClients.class);

	protected final ConsoleInput in;
	protected final JMSClient jmsClient;

	protected AbstractClients(final String topicName, final boolean withReply) throws NamingException, IOException {
		this.in = new ConsoleInput();
		this.jmsClient = new JMSClient(topicName, withReply);
	}

	protected void execute() {
		try {
			boolean repeat = true;
			while (repeat) {
				try {
					repeat = mainMenu();
				} catch (RuntimeException | IOException | JMSException e) {
					e.printStackTrace();
					logger.error("Unexpected exception", e);
					repeat = true;
				}
			}
		} finally {
			jmsClient.close();
		}

	}

	public static String class2Api(Class<?> requestClass) {
		String name = requestClass.getSimpleName();
		if (name.endsWith("Request")) {
			return name.substring(0, name.length() - 7);
		}
		return name;
	}

	protected void sendMessage(final Object body) throws JMSException {
		jmsClient.sendMessage(class2Api(body.getClass()), JSONB.instance().marshal(body));
	}

	protected ELMessage sendReceiveMessage(final Object body) throws JMSException {
		return sendReceiveMessage(class2Api(body.getClass()), body);
	}

	protected ELMessage sendReceiveMessage(final String subject, final Object body) throws JMSException {
		Message response = jmsClient.sendReceiveMessage(subject, JSONB.instance().marshal(body));
		if (response != null) {
			return new ELMessage(response, true);
		}
		return null;
	}

	protected abstract boolean mainMenu() throws IOException, JMSException;
}
