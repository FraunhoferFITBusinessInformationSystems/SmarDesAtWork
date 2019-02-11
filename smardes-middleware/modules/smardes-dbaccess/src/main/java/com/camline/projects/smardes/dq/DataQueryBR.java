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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public class DataQueryBR extends GenericDataQueryBR {
	private final String queryName;

	public DataQueryBR(final String fqQueryName, final int maxResults, final List<Object> bindParametersList) {
		this(fqQueryName, maxResults, bindParametersList, null);
	}

	public DataQueryBR(final String fqQueryName, final int maxResults, final List<Object> bindParametersList,
			final Map<String, Object> bindParametersNamed) {
		super(StringUtils.substringBefore(fqQueryName, "."), maxResults, bindParametersList, bindParametersNamed);
		this.queryName = StringUtils.substringAfter(fqQueryName, ".");
	}

	@Override
	protected String createStatement() {
		final String stmt = DataQueryConfig.instance().getQuery(getModule(), queryName);
		if (stmt == null) {
			throw new SmarDesException(new ErrorResponse(Error.MODULE, Error.QUERY_NOT_FOUND, queryName, null));
		}
		return stmt;
	}

	@Override
	protected String getQueryId() {
		return getModule() + "." + queryName;
	}
}
