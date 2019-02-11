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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;

import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.todo.api.Errors;
import com.camline.projects.smardes.todo.api.GenericInstanceResponse;
import com.camline.projects.smardes.todo.api.ToDoCloseInstanceRequest;
import com.camline.projects.smardes.todo.api.dto.ToDoStepDTO;
import com.camline.projects.smardes.todo.entities.ToDoClosedStep;
import com.camline.projects.smardes.todo.entities.ToDoClosedStepDAO;
import com.camline.projects.smardes.todo.entities.ToDoInstance;
import com.camline.projects.smardes.todo.entities.ToDoInstanceDAO;
import com.camline.projects.smardes.todo.entities.ToDoStepDAO;

final class ToDoCloseInstance extends AbstractToDoHandler<ToDoCloseInstanceRequest> {
	ToDoCloseInstance(final ToDoCloseInstanceRequest request) {
		super(request);
	}

	@Override
	public Object call(final IBR br) {
		ToDoInstance instance = br.createDAO(ToDoInstanceDAO.class)
				.findById(request.getInstanceId(), true);
		ErrorResponse errorResponse = checkInstanceStillOpen(instance, request);
		if (errorResponse != null) {
			return errorResponse;
		}

		if (BooleanUtils.isNotTrue(request.getForce())) {
			List<ToDoClosedStep> closedSteps = br.createDAO(ToDoClosedStepDAO.class).findByInstance(instance, false);
			Set<Integer> closedStepNumbers = closedSteps.stream().map(step -> Integer.valueOf(step.getStep()))
					.collect(Collectors.toSet());

			List<ToDoStepDTO> stepDefs = new ToDoStepDAO().findByTODOList(instance.getDomain(),
					instance.getDefinitionId());
			for (ToDoStepDTO stepDef : stepDefs) {
				if (!closedStepNumbers.contains(Integer.valueOf(stepDef.getStep()))) {
					return new ErrorResponse(Errors.MODULE, Errors.TODO_LIST_NOT_COMPLETED, request.toString(), null);
				}
			}
		}

		instance.close(request.getClosedBy());
		return new GenericInstanceResponse(instance.toDTO(null));
	}
}
