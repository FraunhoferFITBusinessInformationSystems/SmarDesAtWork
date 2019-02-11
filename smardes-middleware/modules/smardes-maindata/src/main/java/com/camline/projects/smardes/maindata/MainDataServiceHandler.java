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

import javax.jms.JMSContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.dq.DataQueryBR;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.jms.CommonErrors;
import com.camline.projects.smardes.jsonapi.jms.JSONServiceHandlerEx;
import com.camline.projects.smardes.maindata.api.MainDataFileRequest;
import com.camline.projects.smardes.maindata.api.MainDataQueryRequest;

public class MainDataServiceHandler extends JSONServiceHandlerEx {
	private static final Logger logger = LoggerFactory.getLogger(MainDataServiceHandler.class);

	private static final String MODULE = "maindata";

	public MainDataServiceHandler(final JMSContext jmsContext) {
		super(jmsContext);

		registerHandler(MainDataQueryRequest.class, MainDataServiceHandler::handleMainDataQuery);
		registerHandler(MainDataFileRequest.class, MainDataServiceHandler::handleMainDataFile);
	}

	public void createConsumer(final String address) {
		setupConsumer(address, null);
	}

	@Override
	protected String getErrorModule() {
		return MODULE;
	}

	private static Object handleMainDataFile(final String bodyText) {
		final MainDataFileRequest fileRequest = JSONB.instance().unmarshalBody(bodyText, MainDataFileRequest.class);
		if (!fileRequest.isValid()) {
			return new ErrorResponse(MODULE, CommonErrors.REQUEST_INVALID,
					String.format("Unsupported or incomplete request: %s", fileRequest.toString()),
					null);
		}
		return BR.execute(logger,
				new MainDataFileBR(fileRequest.getFolders(), fileRequest.getName()));
	}

	private static Object handleMainDataQuery(final String bodyText) {
		final MainDataQueryRequest queryRequest = JSONB.instance().unmarshalBody(bodyText,
				MainDataQueryRequest.class);
		if (!queryRequest.isValid()) {
			return new ErrorResponse(MODULE, CommonErrors.REQUEST_INVALID,
					String.format("Unsupported or incomplete request: %s", queryRequest.toString()),
					null);
		}
		return BR.execute(logger, new DataQueryBR(queryRequest.getQuery(), -1, queryRequest.getPositionalParams(),
				queryRequest.getNamedParams()));
	}
}
