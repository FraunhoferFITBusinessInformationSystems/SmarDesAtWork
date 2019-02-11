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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import javax.jms.Topic;
import javax.naming.NamingException;

public class JMSClient implements AutoCloseable {
	private static final int DEFAULT_TIMEOUT = 60000;

	private static final AtomicLong correlationSeq = new AtomicLong(1);

	private final JMSContext jmsContext;
	private final boolean jmsContextPassed;
	private final Topic topic;
	private final TemporaryQueue replyQueue;
	private final JMSConsumer consumer;

	public JMSClient(final JMSContext jmsContext, final String topicName, final boolean withReply) {
		this.jmsContext = jmsContext;
		this.jmsContextPassed = true;
		this.topic = jmsContext.createTopic(topicName);
		this.replyQueue = withReply ? jmsContext.createTemporaryQueue() : null;
		this.consumer = withReply ? jmsContext.createConsumer(replyQueue) : null;
	}

	public JMSClient(final String topicName, final boolean withReply) throws NamingException, IOException {
		final JMSContextFactory jmsContextFactory = new JMSContextFactory(getClass().getSimpleName() + "-" + topicName);
		this.jmsContext = jmsContextFactory.createContext();
		this.jmsContextPassed = false;
		this.topic = jmsContext.createTopic(topicName);
		this.replyQueue = withReply ? jmsContext.createTemporaryQueue() : null;
		this.consumer = withReply ? jmsContext.createConsumer(replyQueue) : null;
	}

	public JMSClient(final String topicName, Map<String, String> overrideProperties, final boolean withReply)
			throws NamingException, IOException {
		final JMSContextFactory jmsContextFactory = new JMSContextFactory(overrideProperties,
				getClass().getSimpleName() + "-" + topicName);
		this.jmsContext = jmsContextFactory.createContext();
		this.jmsContextPassed = false;
		this.topic = jmsContext.createTopic(topicName);
		this.replyQueue = withReply ? jmsContext.createTemporaryQueue() : null;
		this.consumer = withReply ? jmsContext.createConsumer(replyQueue) : null;
	}

	public void sendMessage(final String subject, final byte[] body) throws JMSException {
		sendMessage(subject, body, Collections.emptyMap());
	}

	public Message sendReceiveMessage(final String subject, final byte[] body) throws JMSException {
		return sendReceiveMessage(subject, body, Collections.emptyMap(), DEFAULT_TIMEOUT);
	}

	public Message sendReceiveMessage(final String subject, final byte[] body, long timeout) throws JMSException {
		return sendReceiveMessage(subject, body, Collections.emptyMap(), timeout);
	}

	public Message sendReceiveMessage(final String subject, final byte[] body, final Map<String, String> properties,
			long timeout) throws JMSException {
		return sendReceiveMessage(subject, body, properties, 0, timeout);
	}

	public Message sendReceiveMessage(final String subject, final byte[] body, final Map<String, String> properties,
			long deliveryDelay, long timeout) throws JMSException {
		final BytesMessage message = createMessage(subject, body, properties);
		return sendReceiveMessage(message, deliveryDelay, timeout);
	}

	public Message sendReceiveMessage(final Message message,
			long deliveryDelay) throws JMSException {
		return sendReceiveMessage(message, deliveryDelay, DEFAULT_TIMEOUT);
	}

	public Message sendReceiveMessage(final Message message,
			long deliveryDelay, long timeout) throws JMSException {
		if (replyQueue != null) {
			final String correlationID = String.valueOf(correlationSeq.getAndIncrement());
			message.setJMSReplyTo(replyQueue);
			message.setJMSCorrelationID(correlationID);
		}

		sendMessage(message, deliveryDelay);

		if (consumer == null) {
			return null;
		}

		final Message response = consumer.receive(timeout);
		MessageLogger.logReceivingReply(response);

		return response;
	}

	public void sendMessage(final String subject, final byte[] body, final Map<String, String> properties)
			throws JMSException {
		final BytesMessage message = createMessage(subject, body, properties);

		sendMessage(message, 0);
	}

	public void sendMessage(final Message message, long deliveryDelay) {
		long realDeliveryDelay;
		if (deliveryDelay > 0) {
			realDeliveryDelay = deliveryDelay;
			MessageLogger.logOutgoingDelayed(message, topic, deliveryDelay);
		} else {
			realDeliveryDelay = Message.DEFAULT_DELIVERY_DELAY;
			MessageLogger.logOutgoing(message, topic);
		}

		jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).setDeliveryDelay(realDeliveryDelay)
				.send(topic, message);
	}

	private BytesMessage createMessage(final String subject, final byte[] body, final Map<String, String> properties)
			throws JMSException {
		final BytesMessage message = jmsContext.createBytesMessage();
		for (final Entry<String, String> entry : properties.entrySet()) {
			message.setStringProperty(entry.getKey(), entry.getValue());
		}
		if (body != null) {
			message.writeBytes(body);
		}
		message.setJMSType(subject);
		return message;
	}

	@Override
	public void close() {
		if (consumer != null) {
			consumer.close();
		}
		if (!jmsContextPassed) {
			jmsContext.close();
		}
	}
}
