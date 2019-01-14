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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.br.BRVoidCallable;
import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.common.el.functions.UUIDFunctions;
import com.camline.projects.smardes.common.jms.JMSClient;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.SmarDesException;

final class TableMonitor implements BRVoidCallable {
	private static final Logger logger = LoggerFactory.getLogger(TableMonitor.class);

	private static final String PERSISTENCE_UNIT = "smardes";

	private final MonitorConfig monitorConfig;
	private final JMSClient jmsClient;

	TableMonitor(final JMSContext jmsContext, final MonitorConfig monitorConfig) {
		this.monitorConfig = monitorConfig;
		this.jmsClient = new JMSClient(jmsContext, monitorConfig.getAddress(), false);
	}

	@Override
	public List<String> getUnits() {
		return Arrays.asList(PERSISTENCE_UNIT);
	}

	@Override
	public void call(final IBR br) {
		final LastEndDAO dao = br.createDAO(LastEndDAO.class);
		LastEnd lastEnd = determineLastEnd(dao);

		if (Thread.currentThread().isInterrupted()) {
			throw new SmarDesException("TableMonitor got an interrupt.");
		}

		final List<Object> bindParameters = determineBindParameters(br, lastEnd);
        final DBMonDataQuery dbMonDataQuery = new DBMonDataQuery(monitorConfig, bindParameters);
		final List<Map<String, Object>> rows = BR.execute(logger, dbMonDataQuery);
        final List<DBMonDataRow> dbMonDataRows = dbMonDataQuery.convert(rows);

		if (lastEnd == null) {
			lastEnd = new LastEnd(monitorConfig.getName());
		}

		int processed = 0;
        for (final DBMonDataRow dbMonDataRow : dbMonDataRows) {
			if (processed >= monitorConfig.getMaxItems()) {
				break;
			}

			try {
                final DBMonEvent dbMonEvent = new DBMonEvent(UUIDFunctions.random(), monitorConfig.getName(),
                        dbMonDataRow.getRow());
				jmsClient.sendMessage(dbMonEvent.getClass().getSimpleName(), JSONB.instance().marshal(dbMonEvent));
			} catch (final JMSException e) {
				throw new SmarDesException("Error in publishing DB monitor event", e);
			}

            lastEnd.setLastObjects(dbMonDataRow.getLastObject1(), dbMonDataRow.getLastObject2(),
                    dbMonDataRow.getLastObject3());

			processed++;
		}

		if (processed > 0) {
			dao.merge(lastEnd);
			br.addContext("lastEndNew", lastEnd.getLastObjects());
		}

		br.addContext("processedRows", processed);
	}

	private LastEnd determineLastEnd(final LastEndDAO dao) {
		LastEnd lastEnd = dao.findByName(monitorConfig.getName(), true);
		if (lastEnd != null) {
			return lastEnd;
		}

		if (monitorConfig.getInitQuery() == null) {
			throw new SmarDesException("No LastEnd found and no init query configured for  " + monitorConfig.getName() + ".");
		}

		logger.info("No LastEnd found. Initializing by init query");

        final DBMonDataQuery dbMonDataQuery = new DBMonDataQuery(monitorConfig);
		final List<Map<String, Object>> rows = BR.execute(logger, dbMonDataQuery);
        final List<DBMonDataRow> dbMonDataRows = dbMonDataQuery.convert(rows);

		lastEnd = new LastEnd(monitorConfig.getName());

        if (dbMonDataRows.size() == 1) {
            final DBMonDataRow dbMonDataRow = dbMonDataRows.get(0);
            lastEnd.setLastObjects(dbMonDataRow.getLastObject1(), dbMonDataRow.getLastObject2(),
                    dbMonDataRow.getLastObject3());
		} else {
			logger.warn("No row found for initial query. Cannot initalize LastEnd with sane start values");
		}

		return dao.merge(lastEnd);
	}

	private List<Object> determineBindParameters(final IBR br, final LastEnd lastEnd) {
		if (lastEnd == null || !lastEnd.isLastObjectAvailable()) {
			return null;
		}

		final List<Object> bindParameters = lastEnd.getLastObjects().stream().filter(Objects::nonNull)
				.collect(Collectors.toList());
		br.addContext("lastEndOld", bindParameters);

		/*
		 * Plausibility check
		 */
		final long numEntries = bindParameters.size();
		if (numEntries == 0) {
			throw new SmarDesException("At least one bind parameter must be set for " + lastEnd.getName() + ".");
		}

		if (monitorConfig.getColumn3() != null) {
			checkMismatchedBindParameters(numEntries, 3);
		} else if (monitorConfig.getColumn2() != null) {
			checkMismatchedBindParameters(numEntries, 2);
		} else if (numEntries != 1) {
			checkMismatchedBindParameters(numEntries, 1);
		}

		return bindParameters;
	}

	private static void checkMismatchedBindParameters(long actual, int expected) {
		if (actual != expected) {
			throw new SmarDesException("Mismatched bindParameter size (" + actual + " != " + expected);
		}
	}
}
