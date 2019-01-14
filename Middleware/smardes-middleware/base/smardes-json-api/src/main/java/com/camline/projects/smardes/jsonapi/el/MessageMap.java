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
package com.camline.projects.smardes.jsonapi.el;

import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.collections.EnumerationIterable;

public final class MessageMap extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(MessageMap.class);

	public MessageMap(final Message message) {
		if (message == null) {
			return;
		}

		Iterable<String> propertyNames;
		try {
			propertyNames = EnumerationIterable.create(message.getPropertyNames());
		} catch (final JMSException e) {
			logger.error("Error getting message property names", e);
			return;
		}

		for (final String propertyName : propertyNames) {
			Object value;
			try {
				value = message.getObjectProperty(propertyName);
			} catch (final JMSException e) {
				logger.error("Error getting message property", e);
				continue;
			}
			put(propertyName, value);
		}
	}
}
