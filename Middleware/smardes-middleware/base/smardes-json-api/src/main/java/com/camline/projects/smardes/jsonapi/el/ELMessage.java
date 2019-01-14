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
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.bind.annotation.JsonbTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.jms.Utils;
import com.camline.projects.smardes.jsonapi.JSONB;

public class ELMessage {
	private static final Logger logger = LoggerFactory.getLogger(ELMessage.class);

	@JsonbTransient
	private final Message raw;
	private final MessageMap properties;
	private final String bodyText;
	private final Map<String, Object> body;

	public ELMessage(final Message message, final boolean bodyIsJSON) {
		this(message, Utils.getBodyAsString(message), bodyIsJSON);
	}

	public ELMessage(final Message message, final String bodyText, final boolean bodyIsJSON) {
		this.raw = message;
		this.properties = new MessageMap(message);
		this.bodyText = bodyText;
		this.body = createBody(bodyText, bodyIsJSON);
	}

	@SuppressWarnings("unchecked")
	public ELMessage(final Message message, final Map<?, ?> body) {
		this.raw = message;
		this.properties = new MessageMap(message);
		this.bodyText = JSONB.instance().marshalToString(body);
		this.body = (Map<String, Object>) body;
	}

	private static Map<String, Object> createBody(final String bodyText, final boolean bodyIsJSON) {
		if (!bodyIsJSON) {
			return null;
		}
		if (bodyText == null) {
			return new HashMap<>();
		}
		return JSONB.instance().unmarshalBodyGeneric(bodyText);
	}

	public Message getRaw() {
		return raw;
	}

	public MessageMap getProp() {
		return properties;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public String getBodyText() {
		return bodyText;
	}

	public String getSubject() {
		try {
			return raw.getJMSType();
		} catch (JMSException e) {
			logger.error("Could not get JMS type aka subject from message. Return null...", e);
			return null;
		}
	}

	@Override
	public String toString() {
		return "ELMessage [bodyText=" + bodyText + "]";
	}


}
