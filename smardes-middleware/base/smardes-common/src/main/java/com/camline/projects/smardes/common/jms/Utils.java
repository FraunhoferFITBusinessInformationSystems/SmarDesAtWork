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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.collections.EnumerationIterable;

public final class Utils {
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	private static final int MAX_BODY_DUMP = 5000;
	private static final int FILL_DOTS = 30;
	private static final String DOTS = ".....................................................................";
	private static final String SEP =
			"\n------------------------------------------------------------------------------------------------";

	private Utils() {
		// utility class
	}

	public static Message newMessage(final JMSContext jmsContext, final Message prototype,
			final String... propertyNames) throws JMSException {
		final BytesMessage message = jmsContext.createBytesMessage();
		for (final String propertyName : propertyNames) {
			message.setStringProperty(propertyName, prototype.getStringProperty(propertyName));
		}
		message.setJMSType(prototype.getJMSType());
		return message;
	}

	private static String createNameEntry(final String name, final Object value) {
		final int numDots = FILL_DOTS - name.length();
		return "\n" + name + DOTS.substring(0, numDots) + ": " + value;
	}

	private static String createNameEntry(final String name, final long value) {
		return createNameEntry(name, String.valueOf(value));
	}

	public static Iterable<String> getOrderedPropertyNames(final Message message) throws JMSException {
		final Iterable<String> propertyNames = EnumerationIterable.create(message.getPropertyNames());
		final List<String> orderedPropertyNames = new ArrayList<>();
		for (final String propertyName : propertyNames) {
			orderedPropertyNames.add(propertyName);
		}
		Collections.sort(orderedPropertyNames);
		return orderedPropertyNames;
	}

	public static String getBodyAsString(final Message message) {
		byte[] body;
		try {
			body = message.getBody(byte[].class);
			if (body != null) {
				/*
				 * We assume an UTF-8 encoded text
				 */
				return new String(body, StandardCharsets.UTF_8);
			}
		} catch (final JMSException e) {
			logger.error("Could not get body from message. Return null.", e);
		}

		return null;
	}

	public static String userDump(final Message message, final String title) {
		Destination destination;
		try {
			destination = message.getJMSDestination();
		} catch (JMSException e) {
			logger.error("Error getting destination from message. Take null.", e);
			destination = null;
		}
		return userDump(message, destination, title);
	}

	public static String userDump(final Message message, Destination destination, final String title) {
		final StringBuilder buf = new StringBuilder();
		buf.append(SEP + "\n" + title + ":");
		try {
			if (destination != null) {
				buf.append(createNameEntry("Destination", destination));
			}
			if (message.getJMSType() != null) {
				buf.append(createNameEntry("JMSType aka Subject", message.getJMSType()));
			}
			if (message.getJMSCorrelationID() != null) {
				buf.append(createNameEntry("JMSCorrelationID", message.getJMSCorrelationID()));
			}

			for (final String propertyName : getOrderedPropertyNames(message)) {
				if (propertyName.startsWith("JMS")) {
					continue;
				}
				buf.append(createNameEntry(propertyName, message.getStringProperty(propertyName)));
			}

			final String body = getBodyAsString(message);
			if (body != null) {
				buf.append(createNameEntry("Body",
						StringUtils.abbreviateMiddle(body, "\n...<omitted>...\n", MAX_BODY_DUMP)));
			}
		} catch (final JMSException | RuntimeException e) {
			logger.error("Could not dump message", e);
			buf.append("\n****** Error in dumping message: " + e.getMessage());
		}
		buf.append(SEP);
		return buf.toString();
	}

	public static String toString(final Message message) {
		final StringBuilder buf = new StringBuilder();
		buf.append(SEP).append("\nNew message:");

		try {
			buf.append(createNameEntry("JMSCorrelationID", message.getJMSCorrelationID()));
			buf.append(createNameEntry("JMSDeliveryMode", message.getJMSDeliveryMode()));
			buf.append(createNameEntry("JMSDeliveryTime", message.getJMSDeliveryTime()));
			buf.append(createNameEntry("JMSExpiration", message.getJMSExpiration()));
			buf.append(createNameEntry("JMSMessageID", message.getJMSMessageID()));
			buf.append(createNameEntry("JMSPriority", message.getJMSPriority()));
			buf.append(createNameEntry("JMSTimestamp", message.getJMSTimestamp()));
			buf.append(createNameEntry("JMSType", message.getJMSType()));
			buf.append(createNameEntry("JMSDestination", message.getJMSDestination()));
			buf.append(createNameEntry("JMSRedelivered", Boolean.valueOf(message.getJMSRedelivered())));
			buf.append(createNameEntry("JMSReplyTo", message.getJMSReplyTo()));

			buf.append("\n\nApplication properties:");
			for (final String propertyName : getOrderedPropertyNames(message)) {
				buf.append(createNameEntry(propertyName, message.getStringProperty(propertyName)));
			}

			final byte[] bb = message.getBody(byte[].class);
			if (bb != null) {
				buf.append("\n\nMessage body [" + bb.length + " bytes]:");
				buf.append(Arrays.toString(bb));
			}
		} catch (final JMSException e) {
			logger.error("Could not dump message", e);
			buf.append("\n****** Error in dumping message: " + e.getMessage());
		}
		buf.append(SEP);
		return buf.toString();
	}
}
