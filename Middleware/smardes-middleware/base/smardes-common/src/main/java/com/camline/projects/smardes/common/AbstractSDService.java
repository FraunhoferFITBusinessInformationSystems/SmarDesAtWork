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
package com.camline.projects.smardes.common;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.jms.AbstractMessageHandler;

/**
 * A SmarDes service consists optionally of a JMS service context for incoming requests,
 * a service handler class that consumes these incoming messages and optionally and
 * outgoing JMS context for outgoing calls.
 *
 * @author matze
 *
 */
public abstract class AbstractSDService implements SDService {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSDService.class);

	private final JMSContext serviceJMSContext;
	private final JMSContext outgoingJMSContext;
	private final List<AbstractMessageHandler> messageHandlers;

	public AbstractSDService(final JMSContext baseJMSContext, boolean needServiceJMSContext,
			boolean needOutgoingJMSContext) {
		this.serviceJMSContext = needServiceJMSContext ? forkContext(baseJMSContext) : null;
		this.outgoingJMSContext = needOutgoingJMSContext ? forkContext(baseJMSContext) : null;

		if (serviceJMSContext != null) {
			logger.info("Service {}: created service context", getClass().getSimpleName());
		}
		if (outgoingJMSContext != null) {
			logger.info("Service {}: created outgoing context", getClass().getSimpleName());
		}
		this.messageHandlers = new ArrayList<>();
	}

	private static JMSContext forkContext(final JMSContext baseJMSContext) {
		return baseJMSContext.createContext(baseJMSContext.getSessionMode());
	}

	protected final JMSContext getServiceJMSContext() {
		return serviceJMSContext;
	}

	protected final JMSContext getOutgoingJMSContext() {
		return outgoingJMSContext;
	}

	protected final void addMessageHandler(AbstractMessageHandler messageHandler) {
		messageHandlers.add(messageHandler);
	}

	protected final void shutdownMessageHandlers() {
		for (AbstractMessageHandler messageHandler : messageHandlers) {
			messageHandler.close();
			logger.info("Service {}: closed message handler {}", getClass().getSimpleName(),
					messageHandler.getClass().getSimpleName());
		}
		messageHandlers.clear();
	}

	@Override
	public void shutdown() {
		shutdownMessageHandlers();
		if (outgoingJMSContext != null) {
			outgoingJMSContext.close();
			logger.info("Service {}: closed outgoing context", getClass().getSimpleName());
		}
		if (serviceJMSContext != null) {
			serviceJMSContext.close();
			logger.info("Service {}: closed service context", getClass().getSimpleName());
		}
	}
}
