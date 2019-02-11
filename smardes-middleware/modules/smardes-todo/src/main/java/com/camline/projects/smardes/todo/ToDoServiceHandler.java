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

import javax.jms.JMSContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.jms.CommonErrors;
import com.camline.projects.smardes.jsonapi.jms.JSONServiceHandlerEx;
import com.camline.projects.smardes.todo.api.Errors;
import com.camline.projects.smardes.todo.api.ToDoAbortInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoCloseInstanceRequest;
import com.camline.projects.smardes.todo.api.ToDoFindRequest;
import com.camline.projects.smardes.todo.api.ToDoFindResponse;
import com.camline.projects.smardes.todo.api.ToDoGetDetailsRequest;
import com.camline.projects.smardes.todo.api.ToDoGetDetailsResponse;
import com.camline.projects.smardes.todo.api.ToDoGetInstanceDetailsRequest;
import com.camline.projects.smardes.todo.api.ToDoGetRunningInstancesRequest;
import com.camline.projects.smardes.todo.api.ToDoMarkStepClosedRequest;
import com.camline.projects.smardes.todo.api.ToDoMarkStepOpenRequest;
import com.camline.projects.smardes.todo.api.ToDoStartInstanceRequest;
import com.camline.projects.smardes.todo.api.dto.ToDoHeaderDTO;
import com.camline.projects.smardes.todo.api.dto.ToDoStepDTO;
import com.camline.projects.smardes.todo.entities.ToDoHeaderDAO;
import com.camline.projects.smardes.todo.entities.ToDoStepDAO;

public class ToDoServiceHandler extends JSONServiceHandlerEx {
	private static final Logger logger = LoggerFactory.getLogger(ToDoServiceHandler.class);

	public ToDoServiceHandler(final JMSContext jmsContext) {
		super(jmsContext);

		registerHandler(ToDoFindRequest.class, ToDoServiceHandler::handleToDoFind);
		registerHandler(ToDoGetDetailsRequest.class, ToDoServiceHandler::handleToDoGetDetails);
		registerHandler(ToDoStartInstanceRequest.class, ToDoServiceHandler::handleToDoStart);
		registerHandler(ToDoGetRunningInstancesRequest.class, ToDoServiceHandler::handleToDoGetRunningInstances);
		registerHandler(ToDoMarkStepClosedRequest.class, ToDoServiceHandler::handleToDoMarkStepClosed);
		registerHandler(ToDoMarkStepOpenRequest.class, ToDoServiceHandler::handleToDoMarkStepOpen);
		registerHandler(ToDoGetInstanceDetailsRequest.class, ToDoServiceHandler::handleToDoGetInstanceDetails);
		registerHandler(ToDoCloseInstanceRequest.class, ToDoServiceHandler::handleToDoCloseInstance);
		registerHandler(ToDoAbortInstanceRequest.class, ToDoServiceHandler::handleToDoAbortInstance);
	}

	public void createConsumer(final String address) {
		setupConsumer(address, null);
	}

	@Override
	protected String getErrorModule() {
		return Errors.MODULE;
	}

	private static Object handleToDoFind(final String bodyText) {
		final ToDoFindRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoFindRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}
		List<ToDoHeaderDTO> headers = new ToDoHeaderDAO().findByContext(request.getDomain(), request.getContext());
		return new ToDoFindResponse(headers);
	}

	private static Object handleToDoGetDetails(final String bodyText) {
		final ToDoGetDetailsRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoGetDetailsRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}

		ToDoHeaderDTO header = new ToDoHeaderDAO().findById(request.getDomain(), request.getId());
		if (header == null) {
			return new ErrorResponse(Errors.MODULE, Errors.AMBIGUOUS_TODO_LIST, request.toString(), null);
		}

		List<ToDoStepDTO> steps = new ToDoStepDAO().findByTODOList(request.getDomain(), request.getId());
		return new ToDoGetDetailsResponse(header, steps);
	}

	private static Object handleToDoStart(final String bodyText) {
		final ToDoStartInstanceRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoStartInstanceRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}
		return BR.execute(logger, new ToDoStartInstance(request));
	}

	private static Object handleToDoGetRunningInstances(final String bodyText) {
		final ToDoGetRunningInstancesRequest request = JSONB.instance().unmarshalBody(bodyText,
				ToDoGetRunningInstancesRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}
		return BR.execute(logger, new ToDoGetRunningInstances(request));
	}

	private static Object handleToDoMarkStepClosed(final String bodyText) {
		final ToDoMarkStepClosedRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoMarkStepClosedRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}
		return BR.execute(logger, new ToDoMarkStepClosed(request));
	}

	private static Object handleToDoMarkStepOpen(final String bodyText) {
		final ToDoMarkStepOpenRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoMarkStepOpenRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}
		return BR.execute(logger, new ToDoMarkStepOpen(request));
	}

	private static Object handleToDoGetInstanceDetails(final String bodyText) {
		final ToDoGetInstanceDetailsRequest request = JSONB.instance().unmarshalBody(bodyText,
				ToDoGetInstanceDetailsRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}

		return BR.execute(logger, new ToDoGetInstanceDetails(request));
	}

	private static Object handleToDoCloseInstance(final String bodyText) {
		final ToDoCloseInstanceRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoCloseInstanceRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}

		return BR.execute(logger, new ToDoCloseInstance(request));
	}

	private static Object handleToDoAbortInstance(final String bodyText) {
		final ToDoAbortInstanceRequest request = JSONB.instance().unmarshalBody(bodyText, ToDoAbortInstanceRequest.class);
		if (!request.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, request.toString(), null);
		}

		return BR.execute(logger, new ToDoAbortInstance(request));
	}
}
