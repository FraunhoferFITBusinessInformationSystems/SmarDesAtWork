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

import javax.jms.Destination;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageLogger {
	private static final String LOGGER_NAME = "com.camline.projects.smardes.MESSAGE";
	private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

	private MessageLogger() {
		// utility class
	}

	public static void logIncoming(Message message) {
		if (logger.isInfoEnabled()) {
			logger.info(Utils.userDump(message, "INCOMING MESSAGE"));
		}
	}

	public static void logOutgoing(Message message, Object address) {
		if (logger.isInfoEnabled()) {
			logger.info(Utils.userDump(message, "SENDING MESSAGE ON ADDRESS " + address));
		}
	}

	public static void logOutgoingDelayed(Message message, Object address, long ms) {
		if (logger.isInfoEnabled()) {
			logger.info(Utils.userDump(message, "SENDING DELAYED (" + ms + " ms) MESSAGE ON ADDRESS " + address));
		}
	}

	public static void logReceivingReply(Message message) {
		if (message == null) {
			logger.warn("No reply received within timeout");
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info(Utils.userDump(message, "RECEIVING REPLY"));
		}
	}

	public static void logSendReply(Message message, boolean success, Object source, Destination destination) {
		if (logger.isInfoEnabled()) {
			logger.info(Utils.userDump(message, destination,
					String.format("Sending %s %s reply", source, success ? "Result" : "Error")));
		}
	}
}
