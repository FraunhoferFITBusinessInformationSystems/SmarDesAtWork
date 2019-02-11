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
package com.camline.projects.smardes.common.jms;

import java.util.concurrent.CountDownLatch;

import javax.jms.BytesMessage;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHandler extends DumpMessageHandler {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);

	static final String SHUTDOWN_TOPIC = "ShutdownComponents";
	static final String PROP_COMPONENT = "Component";


	private final CountDownLatch shutdownSignal;
	private final String component;

	public ShutdownHandler(final JMSContext jmsContext, final String component) {
		super(jmsContext);
		this.shutdownSignal = new CountDownLatch(1);
		this.component = component;
	}

	public void await() throws InterruptedException {
		shutdownSignal.await();
	}

	public void createConsumer() {
		setupConsumer(SHUTDOWN_TOPIC, PROP_COMPONENT + " = '" + component + "'");
	}

	@Override
	public void onMessage(final Message message) {
		super.onMessage(message);

		logger.info("Got shutdown message for component {}", component);

		boolean replyExpected = extractReplyExpected(message);
		if (replyExpected) {
			sendReply(message, null);
		}
		shutdownSignal.countDown();
	}

	protected void sendReply(final Message request, final byte[] body) {
		try {
			final BytesMessage reply = jmsContext.createBytesMessage();
			reply.setJMSCorrelationID(request.getJMSCorrelationID());

			if (body != null) {
				reply.writeBytes(body);
			}

			if (request.getJMSReplyTo() == null) {
				logger.error("Cannot send back response since there is no reply destination");
				return;
			}

			MessageLogger.logSendReply(reply, true, request.getJMSDestination(), request.getJMSReplyTo());
			jmsContext.createProducer().send(request.getJMSReplyTo(), reply);
		} catch (final JMSException | RuntimeException e) {
			logger.error("Cannot send error reply back to client", e);
		}
	}


}
