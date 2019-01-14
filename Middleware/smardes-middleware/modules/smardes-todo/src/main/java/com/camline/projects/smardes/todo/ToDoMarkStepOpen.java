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
import com.camline.projects.smardes.todo.api.ToDoMarkStepOpenRequest;
import com.camline.projects.smardes.todo.entities.ToDoClosedStep;
import com.camline.projects.smardes.todo.entities.ToDoClosedStepDAO;

final class ToDoMarkStepOpen extends AbstractToDoHandler<ToDoMarkStepOpenRequest> {
	ToDoMarkStepOpen(final ToDoMarkStepOpenRequest request) {
		super(request);
	}

	@Override
	public Object call(final IBR br) {
		ToDoClosedStepDAO dao = br.createDAO(ToDoClosedStepDAO.class);
		ToDoClosedStep step = dao.findById(request.getStepId(), true);

		if (step == null) {
			return new ErrorResponse(Errors.MODULE, Errors.UNKNOWN_CLOSED_STEP, request.toString(), null);
		}
		ErrorResponse errorResponse = checkInstanceStillOpen(step.getInstance(), request);
		if (errorResponse != null) {
			return errorResponse;
		}

		dao.remove(step);

		return null;
	}
}
