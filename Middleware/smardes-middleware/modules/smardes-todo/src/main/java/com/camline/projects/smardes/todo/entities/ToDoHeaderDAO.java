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
package com.camline.projects.smardes.todo.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.jdbc.SQLUtils;
import com.camline.projects.smardes.dq.POJODataQueryBR;
import com.camline.projects.smardes.todo.api.dto.ToDoHeaderDTO;

public class ToDoHeaderDAO {
	private static final Logger logger = LoggerFactory.getLogger(ToDoHeaderDAO.class);

	public List<ToDoHeaderDTO> findByContext(String domain, Map<String, String> context) {
		return BR.execute(logger, new FindByContext(domain, context));
	}

	public ToDoHeaderDTO findById(String domain, String id) {
		List<ToDoHeaderDTO> headers = BR.execute(logger, new FindById(domain, id));
		if (headers.size() != 1) {
			return null;
		}
		return headers.get(0);
	}

	private static final class FindByContext extends POJODataQueryBR<ToDoHeaderDTO> {
		private static final List<Pair<String, String>> SELECT_COLUMNS = Arrays.asList(Pair.of("h.todolist", "id"),
				Pair.of("name", null), Pair.of("description", null));

		private final String domain;
		private final Map<String, String> context;

		public FindByContext(String domain, Map<String, String> context) {
			super("maindata", context);
			this.domain = domain;
			this.context = context;
		}

		@Override
		protected String createStatement() {
			String select = SQLUtils.createSelectStatement(SELECT_COLUMNS, domain + "CtxRes c") + " JOIN " + domain
					+ "Header h ON c.todolist = h.todolist" + (context.isEmpty() ? "" : " WHERE ");
			return context.entrySet().stream().map(entry -> entry.getKey() + " = :" + entry.getKey())
					.collect(Collectors.joining(" AND ", select, ""));
		}

		@Override
		protected String getQueryId() {
			return "TODOHeader." + domain;
		}
	}

	private static final class FindById extends POJODataQueryBR<ToDoHeaderDTO> {
		private static final List<Pair<String, String>> SELECT_COLUMNS = Arrays.asList(Pair.of("todolist", "id"),
				Pair.of("name", null), Pair.of("description", null));

		private final String domain;
		private final String id;

		public FindById(final String domain, final String id) {
			super("maindata", Stream.of(Pair.of("id", id)));
			this.domain = domain;
			this.id = id;
		}

		@Override
		protected String createStatement() {
			return SQLUtils.createSelectStatement(SELECT_COLUMNS, domain + "Header") + " WHERE todolist = :id";
		}

		@Override
		protected String getQueryId() {
			return "TODOSteps." + domain + "." + id;
		}
	}
}
