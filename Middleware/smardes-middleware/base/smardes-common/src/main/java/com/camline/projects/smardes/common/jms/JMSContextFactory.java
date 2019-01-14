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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.collections.PropertyUtils;

public class JMSContextFactory implements ExceptionListener {
	private static final Logger logger = LoggerFactory.getLogger(JMSContextFactory.class);

	private static final String PROPERTY_ADDRESS = "address";
	private static final String PROPERTY_USER = "user";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_VHOST = "vhost";
	private static final Set<String> FIXED_PROPERTIES = Stream
			.of(PROPERTY_ADDRESS, PROPERTY_USER, PROPERTY_PASSWORD, PROPERTY_VHOST).collect(Collectors.toSet());

	private final ConnectionFactory connectionFactory;

	public JMSContextFactory(final String clientId)
			throws NamingException, IOException {
		this(Collections.emptyMap(), clientId);
	}

	public JMSContextFactory(Map<String, String> overrideProperties, final String clientId)
			throws NamingException, IOException {
		this(PropertyUtils.loadExternalConfiguration("artemis.properties"), overrideProperties, clientId);
	}

	private JMSContextFactory(final Properties properties, final Map<String, String> overrideProperties,
			final String clientId) throws NamingException {
		ConnectionBuilder connectionBuilder = new ConnectionBuilder(properties.getProperty(PROPERTY_ADDRESS))
				.param("amqp.vhost", properties.getProperty(PROPERTY_VHOST, "default")).clientID(clientId)
				.credentials(properties.getProperty(PROPERTY_USER), properties.getProperty(PROPERTY_PASSWORD));
		properties.entrySet().stream()
				.filter(entry -> !FIXED_PROPERTIES.contains(entry.getKey().toString()) && !Objects.isNull(entry.getValue()))
				.forEach(entry -> connectionBuilder.param(entry.getKey().toString(), entry.getValue().toString()));
		overrideProperties.forEach(connectionBuilder::param);

		final Properties jmsProps = new Properties();
		jmsProps.put("java.naming.factory.initial", "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		jmsProps.put("connectionfactory.ourConnectionfactory", connectionBuilder.build());

		final InitialContext context = new InitialContext(jmsProps);
		connectionFactory = (ConnectionFactory) context.lookup("ourConnectionfactory");
	}

	public JMSContext createContext() {
		final JMSContext jmsContext = connectionFactory.createContext();
		jmsContext.setExceptionListener(this);
		return jmsContext;
	}

	@Override
	public void onException(final JMSException exception) {
		logger.error("Error in JMS", exception);
	}

	static class ConnectionBuilder {
		private final String address;
		private final Map<String, String> params;

		ConnectionBuilder(String address) {
			this.address = address;
			params = new LinkedHashMap<>();
		}

		ConnectionBuilder credentials(String userName, String password) {
			return param("jms.username", userName).param("jms.password", password);
		}

		ConnectionBuilder clientID(String baseClientID) {
			return param("jms.clientID", baseClientID + "-" + System.currentTimeMillis());
		}

		ConnectionBuilder param(String name, String value) {
			params.put(name, value);
			return this;
		}

		String build() {
			return "failover:(amqp://" + address + ")" + params.entrySet().stream()
					.map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&", "?", ""));
		}
	}
}
