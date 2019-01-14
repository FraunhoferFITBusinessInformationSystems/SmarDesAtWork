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

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.transform.Transformers;

import com.camline.projects.smardes.common.br.IBR;

public abstract class POJODataQueryBR<T> extends AbstractDataQueryBR<T> {
	private final Class<T> persistentClass;

	public POJODataQueryBR(final String module, final int maxResults,
			final Map<String, ? extends Object> bindParametersNamed) {
		this(module, maxResults, null, bindParametersNamed);
	}

	public POJODataQueryBR(final String module,
			final Stream<Pair<String, ? extends Object>> bindParametersStream) {
		this(module, -1, null, bindParametersStream.collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
	}

	public POJODataQueryBR(final String module, final int maxResults,
			final Stream<Pair<String, ? extends Object>> bindParametersStream) {
		this(module, maxResults, null, bindParametersStream.collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
	}

	public POJODataQueryBR(final String module,
			final Map<String, ? extends Object> bindParametersNamed) {
		this(module, -1, null, bindParametersNamed);
	}

	public POJODataQueryBR(final String module, final int maxResults, final List<Object> bindParametersList) {
		this(module, maxResults, bindParametersList, null);
	}

	public POJODataQueryBR(final String module, final List<Object> bindParametersList) {
		this(module, -1, bindParametersList, null);
	}

	@SuppressWarnings("unchecked")
	private POJODataQueryBR(final String module, final int maxResults, final List<Object> bindParametersList,
			final Map<String, ? extends Object> bindParametersNamed) {
		super(module, maxResults, bindParametersList, bindParametersNamed);
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@Override
	protected Query createQuery(final IBR br, final String stmt) {
		Query query = super.createQuery(br, stmt);

		/*
		 * Hibernate queries allow to return a Map for each row.
		 * Unfortunately hibernate marked it deprecated without providing an alternative.
		 */
		query.unwrap(org.hibernate.query.Query.class).setResultTransformer(Transformers.aliasToBean(persistentClass));

		return query;
	}
}
