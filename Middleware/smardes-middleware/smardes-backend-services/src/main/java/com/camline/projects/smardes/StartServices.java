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
package com.camline.projects.smardes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.SDService;
import com.camline.projects.smardes.common.jms.JMSContextFactory;
import com.camline.projects.smardes.common.jms.ShutdownHandler;
import com.camline.projects.smardes.common.jpa.DAOFactory;
import com.camline.projects.smardes.db.DatabaseServer;
import com.camline.projects.smardes.db.JPAService;
import com.camline.projects.smardes.dbmon.DBMonitoringService;
import com.camline.projects.smardes.dq.DataQueryConfig;
import com.camline.projects.smardes.maindata.MainDataService;
import com.camline.projects.smardes.resource.ResourceService;
import com.camline.projects.smardes.rule.RuleEngine;
import com.camline.projects.smardes.todo.ToDoService;

public final class StartServices {
	private static final Logger logger = LoggerFactory.getLogger(StartServices.class);

	private StartServices() {
		// just the main entry class
	}

	private static void doRun() throws NamingException, IOException {
		logger.info("Start SmarDes services...");

		final JMSContextFactory jmsContextFactory = new JMSContextFactory(StartServices.class.getSimpleName());

		List<SDService> services = null;
		try (JMSContext jmsContext = jmsContextFactory.createContext()) {
			services = Arrays.asList(
					new DatabaseServer(),
					new JPAService(),
					DataQueryConfig.instance(),
					new MainDataService(jmsContext),
					new ToDoService(jmsContext),
					new ResourceService(jmsContext),
					new RuleEngine(jmsContext),
					new DBMonitoringService(jmsContext));
			for (final SDService service : services) {
				logger.info("Starting service {}...", service.getClass().getSimpleName());
				try {
					service.startup();
				} catch (RuntimeException e) {
					logger.error("Problems initializing service {}", service.getClass().getSimpleName(), e);
					throw e;
				}
			}

			final ShutdownHandler shutdownHandler = new ShutdownHandler(jmsContext, StartServices.class.getSimpleName());
			shutdownHandler.createConsumer();

			try {
				shutdownHandler.await();
			} catch (@SuppressWarnings("unused") final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			logger.info("Initiating shutdown...");
		} finally {
			if (services != null) {
				Collections.reverse(services);
				services.forEach(SDService::shutdown);
			}
			DAOFactory.closeAll();
		}
	}

	public static void main(final String[] args) {
		try {
			doRun();
		} catch (NamingException | IOException | RuntimeException e) {
			logger.error("SmarDes Backend Services Error", e);
		}
	}
}
