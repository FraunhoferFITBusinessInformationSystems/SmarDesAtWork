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
package com.camline.projects.smardes.jsonapi.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.jms.DumpMessageHandler;
import com.camline.projects.smardes.common.jms.MessageLogger;
import com.camline.projects.smardes.common.jms.Utils;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.Response;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public abstract class JSONServiceHandler extends DumpMessageHandler {
	private static final Logger logger = LoggerFactory.getLogger(JSONServiceHandler.class);

	public enum BodyType {
		UNKNOWN,
		TEXT,
		JSON
	}

	public JSONServiceHandler(final JMSContext jmsContext) {
		super(jmsContext);
	}

	/**
	 * If a handler does not support JSON then override this method and return false.
	 *
	 * @param message current message object
	 */
	protected BodyType getBodyType(final Message message) {
		return BodyType.JSON;
	}

	@Override
	public final void onMessage(final Message message) {
		super.onMessage(message);

		boolean replyExpected = extractReplyExpected(message);

		try {
			Object result;

			switch(getBodyType(message)) {
			case TEXT:
			case JSON:
				final String bodyText = Utils.getBodyAsString(message);
				if (bodyText != null) {
					result = doMessage(message, bodyText, replyExpected);
				} else {
					result = new ErrorResponse(getErrorModule(), CommonErrors.NO_REQUEST_BODY,
								"Request message contains no text body", null);
				}
				break;
			default:
				result = doMessage(message, null, replyExpected);
				break;
			}

			if (replyExpected) {
				sendReply(message, result);
			}
		} catch (final JMSException e) {
			logger.error("JMS exception in onMessage", e);
			final ErrorResponse errorResponse = new ErrorResponse(getErrorModule(), CommonErrors.JMS_ERROR,
					"Error in messaging layer. See exception for details", e);
			if (replyExpected) {
				sendReply(message, errorResponse);
			}
		} catch (final SmarDesException e) {
			logger.error("SmarDesException in onMessage", e);
			if (e.getErrorResponse() != null) {
				sendReply(message, e.getErrorResponse());
			} else {
				final ErrorResponse errorResponse = new ErrorResponse(getErrorModule(), CommonErrors.UNKNOWN,
						"Unexpected error. See exception for details", e);
				sendReply(message, errorResponse);
			}
		} catch (final RuntimeException e) {
			logger.error("Unexpected exception in onMessage", e);
			final ErrorResponse errorResponse = new ErrorResponse(getErrorModule(), CommonErrors.UNKNOWN,
					"Unexpected error. See exception for details", e);
			sendReply(message, errorResponse);
		}
	}

	protected abstract String getErrorModule();

	protected abstract Object doMessage(final Message message, final String bodyText, boolean replyExpected)
			throws JMSException;

	protected void sendReply(final Message request, final Object result) {
		try {
			final byte[] payload = createResponsePayload(result);

			final BytesMessage reply = jmsContext.createBytesMessage();
			reply.setJMSCorrelationID(request.getJMSCorrelationID());
			reply.writeBytes(payload);
			reply.setJMSType(request.getJMSType());


			if (request.getJMSReplyTo() == null) {
				logger.error("Cannot send back response since there is no reply destination");
				return;
			}

			MessageLogger.logSendReply(reply, !(result instanceof ErrorResponse), request.getJMSDestination(),
					request.getJMSReplyTo());
			jmsContext.createProducer().send(request.getJMSReplyTo(), reply);
		} catch (final JMSException | RuntimeException ex) {
			logger.error("Cannot send error reply back to client", ex);
		}
	}

	private static byte[] createResponsePayload(final Object result) {
		if (result == null) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (result instanceof byte[]) {
			return (byte[]) result;
		}
		return JSONB.instance().marshal(new Response(result));
	}
}
