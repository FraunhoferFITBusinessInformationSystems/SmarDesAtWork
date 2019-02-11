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
package com.camline.projects.smardes.maindata;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jms.JMSContext;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.AbstractSDService;
import com.camline.projects.smardes.common.io.AbstractDirectoryMonitor;
import com.camline.projects.smardes.maindata.csv.CSVFileImporter;
import com.camline.projects.smardes.maindata.csv.MainDataCSVImporter;

public class MainDataService extends AbstractSDService {
	private static final Logger logger = LoggerFactory.getLogger(MainDataService.class);

	private static final int DIR_MONITOR_INTERVAL = 20;

	static final String PERSISTENCE_UNIT = "maindata";

	private final ScheduledExecutorService mdMonitor;
	private List<ScheduledExecutorService> csvFileMonitors;

	public MainDataService(final JMSContext baseJMSContext) {
		super(baseJMSContext, true, true);
		this.mdMonitor = createScheduledExecutorService("mdMonitor");
	}

	private static ScheduledExecutorService createScheduledExecutorService(String threadName) {
		return Executors.newSingleThreadScheduledExecutor(
				new BasicThreadFactory.Builder().namingPattern(threadName).build());
	}

	@Override
	public void startup() {
		List<Runnable> directoryMonitors = Stream.concat(
				MainDataConfig.instance().getXLSXFolders().stream().map(MainDataXLSImporter::new),
				MainDataConfig.instance().getCSVFolders().stream().map(MainDataCSVImporter::new))
				.collect(Collectors.toList());
		AbstractDirectoryMonitor.runImmediateThenScheduled(mdMonitor, DIR_MONITOR_INTERVAL, directoryMonitors);

		csvFileMonitors = MainDataConfig.instance().getCSVFileMonitorConfigs().stream()
				.map(config -> new CSVFileImporter(getOutgoingJMSContext(), config))
				.map(importer -> importer.runImmediateThenScheduled(
						createScheduledExecutorService("csvFileMonitor-" + importer.getName())))
				.collect(Collectors.toList());

		MainDataServiceHandler handler = new MainDataServiceHandler(getServiceJMSContext());
		handler.createConsumer(MainDataConfig.instance().getMainDataAddress());
		addMessageHandler(handler);
		logger.info("MainDataServiceHandler now listens for MainData requests.");
	}

	@Override
	public void shutdown() {
		mdMonitor.shutdownNow();

		if (csvFileMonitors != null) {
			csvFileMonitors.forEach(ScheduledExecutorService::shutdownNow);
		}

		super.shutdown();
	}
}
