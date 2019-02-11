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

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageHandler implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(AbstractMessageHandler.class);

	protected final JMSContext jmsContext;
	private JMSConsumer consumer;

	public AbstractMessageHandler(final JMSContext jmsContext) {
		this.jmsContext = jmsContext;
	}

	protected void setupConsumer(final String address, final String selectorCriteria) {
		if (consumer != null) {
			throw new IllegalStateException("A consumer was already set up");
		}

		final Topic topic = jmsContext.createTopic(address);
		consumer = jmsContext.createConsumer(topic, selectorCriteria);
		consumer.setMessageListener(this);
		if (selectorCriteria != null) {
			logger.info("Now listen on topic \"{}\" with condition \"{}\"", address, selectorCriteria);
		} else {
			logger.info("Now listen to all messages on topic \"{}\"", address);
		}
	}

	protected static boolean extractReplyExpected(final Message message) {
		try {
			return message.getJMSReplyTo() != null;
		} catch (JMSException e) {
			logger.warn("Error in getJMSReplyTo() - setting replyExpected to false", e);
			return false;
		}
	}

	public void close() {
		if (consumer != null) {
			consumer.close();
		}
	}
}
