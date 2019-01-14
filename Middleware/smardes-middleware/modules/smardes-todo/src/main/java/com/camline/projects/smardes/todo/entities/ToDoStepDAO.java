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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.jdbc.SQLUtils;
import com.camline.projects.smardes.dq.POJODataQueryBR;
import com.camline.projects.smardes.todo.api.dto.ToDoStepDTO;

public class ToDoStepDAO {
	private static final Logger logger = LoggerFactory.getLogger(ToDoStepDAO.class);

	public List<ToDoStepDTO> findByTODOList(String domain, String todoListId) {
		return BR.execute(logger, new FindByTODOList(domain, todoListId));
	}

	public ToDoStepDTO findByTODOListStep(String domain, String todoListId, int step) {
		List<ToDoStepDTO> steps = BR.execute(logger, new FindByTODOListStep(domain, todoListId, step));
		if (steps.size() != 1) {
			return null;
		}
		return steps.get(0);
	}

	private static final class FindByTODOList extends POJODataQueryBR<ToDoStepDTO> {
		private static final List<Pair<String, String>> SELECT_COLUMNS = Arrays
				.asList("step", "name", "description", "resource").stream().map(name -> Pair.of(name, (String) null))
				.collect(Collectors.toList());

		private final String domain;
		private final String todoListId;

		public FindByTODOList(String domain, String todoListId) {
			super("maindata", Stream.of(Pair.of("id", todoListId)));
			this.domain = domain;
			this.todoListId = todoListId;
		}

		@Override
		protected String createStatement() {
			return SQLUtils.createSelectStatement(SELECT_COLUMNS,
					domain + "Steps") + " WHERE todolist = :id";
		}

		@Override
		protected String getQueryId() {
			return "TODOSteps." + domain + "." + todoListId;
		}
	}

	private static final class FindByTODOListStep extends POJODataQueryBR<ToDoStepDTO> {
		private static final List<Pair<String, String>> SELECT_COLUMNS = Arrays.asList(
				Pair.of("step", null), Pair.of("name", null), Pair.of("description", null));

		private final String domain;
		private final String todoListId;
		private final int step;

		public FindByTODOListStep(String domain, String todoListId, int step) {
			super("maindata", Stream.of(Pair.of("id", todoListId), Pair.of("step", Integer.valueOf(step))));
			this.domain = domain;
			this.todoListId = todoListId;
			this.step = step;
		}

		@Override
		protected String createStatement() {
			return SQLUtils.createSelectStatement(SELECT_COLUMNS,
					domain + "Steps") + " WHERE todolist = :id and step = :step";
		}

		@Override
		protected String getQueryId() {
			return "TODOSteps." + domain + "." + todoListId + "." + step;
		}
	}
}
