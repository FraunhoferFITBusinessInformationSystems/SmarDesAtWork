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
package com.camline.projects.smardes.todo;

import java.util.Arrays;
import java.util.List;

import com.camline.projects.smardes.common.br.BRCallable;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.todo.api.Errors;
import com.camline.projects.smardes.todo.entities.ToDoInstance;

abstract class AbstractToDoHandler<R> implements BRCallable<Object> {
	protected final R request;

	public AbstractToDoHandler(R request) {
		this.request = request;
	}

	@Override
	public List<String> getUnits() {
		return Arrays.asList(ToDoService.PERSISTENCE_UNIT);
	}

	protected static ErrorResponse checkInstanceStillOpen(ToDoInstance instance, Object request) {
		if (instance == null) {
			return new ErrorResponse(Errors.MODULE, Errors.UNKNOWN_TODO_LIST_INSTANCE, request.toString(), null);
		}
		if (instance.getClosedAt() != null) {
			return new ErrorResponse(Errors.MODULE, Errors.TODO_LIST_ALREADY_CLOSED, request.toString(), null);
		}
		if (instance.getAbortedAt() != null) {
			return new ErrorResponse(Errors.MODULE, Errors.TODO_LIST_ALREADY_ABORTED, request.toString(), null);
		}
		return null;
	}
}
