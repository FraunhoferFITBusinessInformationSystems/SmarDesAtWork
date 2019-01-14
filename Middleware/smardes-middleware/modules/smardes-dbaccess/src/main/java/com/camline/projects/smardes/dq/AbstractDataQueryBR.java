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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.camline.projects.smardes.common.br.BRCallable;
import com.camline.projects.smardes.common.br.IBR;

public abstract class AbstractDataQueryBR<T> implements BRCallable<List<T>> {
	private final String module;
	private final int maxResults;
	private final List<Object> bindParametersList;
	protected final Map<String, ? extends Object> bindParametersNamed;

	public AbstractDataQueryBR(final String module, final int maxResults, final List<Object> bindParametersList,
			final Map<String, ? extends Object> bindParametersNamed) {
		this.module = module;
		this.maxResults = maxResults;
		this.bindParametersList = bindParametersList;
		this.bindParametersNamed = bindParametersNamed;
	}

	@Override
	public List<String> getUnits() {
		return Arrays.asList(module);
	}

	protected String getModule() {
		return module;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> call(final IBR br) {
		final String stmt = createStatement();

		final Query query = createQuery(br, stmt);
		if (maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		br.addContext("query", getQueryId());

		if (bindParametersNamed != null) {
			bindParametersNamed.entrySet().forEach(entry -> query.setParameter(entry.getKey(), entry.getValue()));
		}

		if (bindParametersList != null) {
			for (int i = 0; i < bindParametersList.size(); i++) {
				query.setParameter(i+1, bindParametersList.get(i));
			}
		}

		final List<?> result = query.getResultList();
		br.addContext("rows", result.size());

		return (List<T>) result;
	}

	protected abstract String createStatement();
	protected abstract String getQueryId();

	protected Query createQuery(final IBR br, final String stmt) {
		return br.getEntityManager().createNativeQuery(stmt);
	}
}
