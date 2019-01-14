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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.todo.api.ToDoFindRequest;
import com.camline.projects.smardes.todo.api.ToDoGetRunningInstancesResponse;
import com.camline.projects.smardes.todo.api.dto.ToDoHeaderDTO;
import com.camline.projects.smardes.todo.api.dto.ToDoInstanceDTO;
import com.camline.projects.smardes.todo.entities.ToDoHeaderDAO;
import com.camline.projects.smardes.todo.entities.ToDoInstance;
import com.camline.projects.smardes.todo.entities.ToDoInstanceDAO;

final class ToDoGetRunningInstances extends AbstractToDoHandler<ToDoFindRequest> {
	ToDoGetRunningInstances(final ToDoFindRequest request) {
		super(request);
	}

	@Override
	public Object call(final IBR br) {
		List<ToDoHeaderDTO> headers = new ToDoHeaderDAO().findByContext(request.getDomain(), request.getContext());
		Map<String, ToDoHeaderDTO> headerMap = headers.stream()
				.collect(Collectors.toMap(ToDoHeaderDTO::getId, Function.identity()));

		List<ToDoInstance> allInstances = br.createDAO(ToDoInstanceDAO.class).findRunningInstances();

		List<ToDoInstanceDTO> instances = new ArrayList<>();
		for (ToDoInstance instance : allInstances) {
			ToDoHeaderDTO header = headerMap.get(instance.getDefinitionId());
			instances.add(instance.toDTO(header));
		}

		return new ToDoGetRunningInstancesResponse(instances);
	}
}
