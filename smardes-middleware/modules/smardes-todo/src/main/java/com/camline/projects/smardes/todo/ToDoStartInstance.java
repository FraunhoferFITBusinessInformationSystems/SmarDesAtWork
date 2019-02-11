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

import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.todo.api.Errors;
import com.camline.projects.smardes.todo.api.ToDoStartInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoStartInstanceResponse;
import com.camline.projects.smardes.todo.api.dto.ToDoHeaderDTO;
import com.camline.projects.smardes.todo.entities.ToDoHeaderDAO;
import com.camline.projects.smardes.todo.entities.ToDoInstance;
import com.camline.projects.smardes.todo.entities.ToDoInstanceDAO;

final class ToDoStartInstance extends AbstractToDoHandler<ToDoStartInstanceRequest> {
	ToDoStartInstance(final ToDoStartInstanceRequest request) {
		super(request);
	}

	@Override
	public Object call(final IBR br) {
		ToDoHeaderDTO header = new ToDoHeaderDAO().findById(request.getDomain(), request.getDefinitionId());
		if (header == null) {
			return new ErrorResponse(Errors.MODULE, Errors.AMBIGUOUS_TODO_LIST, request.toString(), null);
		}

		ToDoInstance instance = new ToDoInstance(request.getDomain(), request.getDefinitionId(),
				request.getStartedBy(), request.getContext());

		ToDoInstanceDAO dao = br.createDAO(ToDoInstanceDAO.class);
		instance = dao.merge(instance);

		return new ToDoStartInstanceResponse(header, instance.getUuid());
	}
}
