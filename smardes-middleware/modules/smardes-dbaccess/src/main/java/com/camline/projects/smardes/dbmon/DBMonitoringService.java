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
package com.camline.projects.smardes.dbmon;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.JMSContext;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.AbstractSDService;
import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.common.concurrent.CatchingRunnable;
import com.camline.projects.smardes.common.io.FullDirectoryMonitor;

public class DBMonitoringService extends AbstractSDService {
	private static final Logger logger = LoggerFactory.getLogger(DBMonitoringService.class);

	private static final int DIR_MONITOR_INTERVAL = 20;

	private final ScheduledExecutorService configMonitor;

	private ScheduledExecutorService executorService;

	public DBMonitoringService(final JMSContext baseJMSContext) {
		super(baseJMSContext, false, true);

		this.configMonitor = Executors.newSingleThreadScheduledExecutor(
				new BasicThreadFactory.Builder().namingPattern("dbmonMonitor").build());
	}

	@Override
	public void startup() {
		final ConfigLoader configLoader = new ConfigLoader();
		configLoader.runImmediateThenScheduled(configMonitor, DIR_MONITOR_INTERVAL);
	}

	@Override
	public void shutdown() {
		configMonitor.shutdownNow();
		shutdownExecutor();
		super.shutdown();
	}

	private void shutdownExecutor() {
		if (executorService != null) {
			executorService.shutdownNow();
			try {
				final boolean terminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
				if (terminated) {
					logger.info("DB monitoring terminated properly.");
				} else {
					logger.info("DB monitoring did not terminated within given shutdown time frame.");
				}
			} catch (@SuppressWarnings("unused") final InterruptedException e) {
				logger.warn("DB monitoring termination was interrupted.");
				Thread.currentThread().interrupt();
			}
		}
	}

	class ConfigLoader extends FullDirectoryMonitor {
		public ConfigLoader() {
			super(new File(PropertyUtils.CONFIG_DIR, "dbmon"));
		}

		@Override
		public boolean accept(final File dir, final String name) {
			return name.endsWith(".properties");
		}

		@Override
		protected void processDirectory() throws IOException {
			shutdownExecutor();

			Map<Object, Object> allProperties = PropertyUtils.mergeProperties(directory.listFiles(this));

			final Map<String, Map<String, String>> monitors = PropertyUtils.groupProperties(allProperties, "dbmon");
			final List<MonitorConfig> monitorConfigs = monitors.entrySet().stream()
					.map(entry -> new MonitorConfig(entry.getKey(), entry.getValue()))
					.filter(MonitorConfig::isActive)
					.collect(Collectors.toList());

			executorService = Executors
					.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder().namingPattern("dbmon-%d").build());
			monitorConfigs.forEach(config -> executorService.scheduleWithFixedDelay(
					new CatchingRunnable(() -> BR.execute(logger, new TableMonitor(getOutgoingJMSContext(), config))), 0,
					config.getInterval(), TimeUnit.MILLISECONDS));

			logger.info("DB monitoring on {} tables started.", Integer.valueOf(monitorConfigs.size()));
		}
	}
}
