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
package com.camline.projects.smardes.dq;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.SDService;
import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.common.io.FullDirectoryMonitor;

public final class DataQueryConfig implements SDService {
	private static final Logger logger = LoggerFactory.getLogger(DataQueryConfig.class);
	private static final String PROP_QUERY_PREFIX = "query";

	private static final int DIR_MONITOR_INTERVAL = 20;

	private static final DataQueryConfig instance = new DataQueryConfig();

	private final ScheduledExecutorService dqMonitor;

	/*
	 * allQueries is updated by one thread with one assignment. The old value is
	 * not used for creating the new value.
	 * Therefore "volatile" fits for this kind of concurrent access.
	 */
	private volatile Map<String, Map<String, String>> allQueries;

	private DataQueryConfig() {
		this.dqMonitor = Executors.newSingleThreadScheduledExecutor(
				new BasicThreadFactory.Builder().namingPattern("dqMonitor").build());
	}

	public static DataQueryConfig instance() {
		return instance;
	}

	@Override
	public void startup() {
		final ConfigLoader configLoader = new ConfigLoader();
		configLoader.runImmediateThenScheduled(dqMonitor, DIR_MONITOR_INTERVAL);
	}

	@Override
	public void shutdown() {
		dqMonitor.shutdownNow();
	}

	public String getQuery(final String module, final String queryName) {
		final Map<String, String> queries = allQueries.get(module);
		if (queries == null) {
			return null;
		}
		return queries.get(queryName);
	}

	class ConfigLoader extends FullDirectoryMonitor {

		public ConfigLoader() {
			super(new File(PropertyUtils.CONFIG_DIR, "queries"));
		}

		@Override
		public boolean accept(final File dir, final String name) {
			return name.endsWith(".properties");
		}

		@Override
		protected void processDirectory() throws IOException {
			Map<Object, Object> allProperties = PropertyUtils.mergeProperties(directory.listFiles(this));

			/*
			 * This is the only place where we assign allQueries and this is only called
			 * by the background thread that monitors the properties file.
			 */
			allQueries = PropertyUtils.groupProperties(allProperties, PROP_QUERY_PREFIX);
			allQueries.forEach((module, queries) -> queries.forEach((id, stmt) -> {
				if (stmt.trim().endsWith(";")) {
					logger.warn("Data Query {}.{}.{} ends with semicolon", PROP_QUERY_PREFIX, module, id);
				}
			}));
		}
	}
}
