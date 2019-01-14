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
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownComponent {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownComponent.class);
	private static final long RECEIVE_TIMEOUT = 5000;
	private static final long MAX_RECONNECT_ATTEMPTS = 10;

	public static void main(final String[] args) {
		if (args.length < 1) {
			logger.error("Usage: {} <component>...", ShutdownComponent.class.getName());
			System.exit(1);
		}

		try {
			boolean allSuccessful = true;

			Map<String, String> overrideProperties = new HashMap<>();
			overrideProperties.put("failover.maxReconnectAttempts", String.valueOf(MAX_RECONNECT_ATTEMPTS));

			try (JMSClient jmsClient = new JMSClient(ShutdownHandler.SHUTDOWN_TOPIC, overrideProperties, true)) {
				for (final String arg : args) {
					allSuccessful &= sendShutdown(arg, jmsClient);
				}
			}
			System.exit(allSuccessful ? 0 : 1);
		} catch (IOException | NamingException | JMSException | RuntimeException e) {
			logger.error("ShutdownComponent error", e);
			System.exit(1);
		}
	}

	private static boolean sendShutdown(final String component, final JMSClient jmsClient) throws JMSException {
		final Map<String, String> properties = new HashMap<>();
		properties.put(ShutdownHandler.PROP_COMPONENT, component);
		Message reply = jmsClient.sendReceiveMessage("", null, properties, RECEIVE_TIMEOUT);
		if (reply != null) {
			logger.info("Shutdown for {} successfully sent.", component);
			return true;
		}
		return false;
	}
}
