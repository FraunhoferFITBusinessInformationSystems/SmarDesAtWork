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
package com.camline.projects.smardes.resource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSContext;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.AbstractSDService;
import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.concurrent.CatchingRunnable;
import com.camline.projects.smardes.common.jpa.DAOFactory;

public class ResourceService extends AbstractSDService {
	private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

	private static final int LIFETIME_PERCENTAGE = 10;

	static final String PERSISTENCE_UNIT = "smardes";

	private ScheduledExecutorService executorService;

	public ResourceService(final JMSContext baseJMSContext) {
		super(baseJMSContext, true, false);
	}

	@Override
	public void startup() {
		logger.info("Test DB connection...");
		DAOFactory.getDAOFactory(PERSISTENCE_UNIT);

		ResourceConfig config = ResourceConfig.instance();

		executorService = Executors.newSingleThreadScheduledExecutor(
				new BasicThreadFactory.Builder().namingPattern("resourceGC").build());
		executorService.scheduleWithFixedDelay(
				new CatchingRunnable(() -> BR.execute(logger, new ResourceGC(config.getResourceDir(), config.getKeepSeconds()))),
				0, config.getKeepSeconds() / LIFETIME_PERCENTAGE, TimeUnit.SECONDS);

		ResourceServiceHandler handler = new ResourceServiceHandler(getServiceJMSContext());
		handler.createConsumer(config.getAddress());
		addMessageHandler(handler);

		logger.info("ResourceServiceHandler now listens for Resource requests.");
	}

	@Override
	public void shutdown() {
		if (executorService != null) {
			executorService.shutdownNow();
			logger.info("Resource GC stopped.");
		}
		super.shutdown();
	}
}
