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
package com.camline.projects.smardes.maindata.csv;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import com.camline.projects.smardes.common.concurrent.CatchingRunnable;
import com.camline.projects.smardes.common.el.functions.UUIDFunctions;
import com.camline.projects.smardes.common.io.FileMonitor;
import com.camline.projects.smardes.common.jms.JMSClient;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public class CSVFileImporter extends FileMonitor<String>{
	private final CSVFileMonitorConfig config;
    private final JMSClient jmsClient;

	public CSVFileImporter(JMSContext jmsContext, CSVFileMonitorConfig config) {
		super(config.getUrl());
		this.config = config;
		if (config.getAddress() != null) {
			this.jmsClient = new JMSClient(jmsContext, config.getAddress(), true);
		} else {
			this.jmsClient = null;
		}
	}

	@Override
	protected String processContent(byte[] content, String oldResult) throws IOException {
		StringReader reader = new StringReader(new String(content,  config.getCharset()));
		CSVToDB csvToDB = new CSVToDB();
		csvToDB.execute(reader, config.getName(), config.getName(), config.getTableName(), config.getLocale(),
				config.getDelimiter());

        sendCSVFileEvent();

		return config.getName();
	}

	private void sendCSVFileEvent() {
		if (jmsClient == null) {
			// No address configured
			return;
		}

		try {
            final CSVFileEvent csvFileEvent = new CSVFileEvent(UUIDFunctions.random(), config.getName(),
                    config.getUrl().toString());
			jmsClient.sendReceiveMessage(csvFileEvent.getClass().getSimpleName(),
					JSONB.instance().marshal(csvFileEvent));

        } catch (final JMSException e) {
            throw new SmarDesException("Error in publishing DB monitor event", e);
        }
	}

	public String getName() {
		return config.getName();
	}

	public ScheduledExecutorService runImmediateThenScheduled(final ScheduledExecutorService executorService) {
		executorService.scheduleWithFixedDelay(new CatchingRunnable(this), 0, config.getInterval(), TimeUnit.SECONDS);
		return executorService;
	}
}
