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
package com.camline.projects.smardes.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.br.BRCallable;
import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.jsonapi.ErrorResponse;
import com.camline.projects.smardes.jsonapi.JSONB;
import com.camline.projects.smardes.jsonapi.jms.CommonErrors;
import com.camline.projects.smardes.jsonapi.jms.JSONServiceHandlerEx;
import com.camline.projects.smardes.resources.api.GetFileRequest;
import com.camline.projects.smardes.resources.api.GetFileResponse;
import com.camline.projects.smardes.resources.api.GetResourceRequest;
import com.camline.projects.smardes.resources.api.GetResourceResponse;
import com.camline.projects.smardes.resources.api.PutResourceRequest;
import com.camline.projects.smardes.resources.api.PutResourceResponse;

public class ResourceServiceHandler extends JSONServiceHandlerEx {
	private static final Logger logger = LoggerFactory.getLogger(ResourceServiceHandler.class);

	public ResourceServiceHandler(final JMSContext jmsContext) {
		super(jmsContext);

		registerHandler(GetFileRequest.class, ResourceServiceHandler::handleGetFile);
		registerHandler(GetResourceRequest.class, ResourceServiceHandler::handleGetResource);
		registerHandler(PutResourceRequest.class, ResourceServiceHandler::handlePutResource);
	}

	public void createConsumer(final String address) {
		setupConsumer(address, null);
	}

	@Override
	protected String getErrorModule() {
		return Errors.MODULE;
	}

	private static Object handleGetFile(final String bodyText) {
		final GetFileRequest getRequest = JSONB.instance().unmarshalBody(bodyText, GetFileRequest.class);
		if (!getRequest.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, getRequest.toString(), null);
		}

		return BR.execute(logger, new GetFile(getRequest));
	}

	private static final class GetFile implements BRCallable<Object> {
		private final GetFileRequest request;

		private GetFile(final GetFileRequest request) {
			this.request = request;
		}

		@Override
		public List<String> getUnits() {
			return Collections.emptyList();
		}

		@Override
		public Object call(final IBR br) {
			Path path = Paths.get(ResourceConfig.instance().getFilesDir(), request.getPath());

			String mimeType;
			byte[] body;
			long modificationTime;
			try {
				mimeType = Files.probeContentType(path);
				body = Files.readAllBytes(path);
				modificationTime = Files.getLastModifiedTime(path).toMillis();
			} catch (IOException e) {
				return new ErrorResponse(Errors.MODULE, Errors.IO_PROBLEM_WITH_DROPIN_FILE, request.toString(), e);
			}

			return new GetFileResponse(modificationTime, mimeType, body);
		}
	}

	private static Object handleGetResource(final String bodyText) {
		final GetResourceRequest getRequest = JSONB.instance().unmarshalBody(bodyText, GetResourceRequest.class);
		if (!getRequest.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, getRequest.toString(), null);
		}
		return BR.execute(logger, new GetResource(getRequest));
	}

	private static final class GetResource implements BRCallable<Object> {
		private final GetResourceRequest request;

		private GetResource(final GetResourceRequest request) {
			this.request = request;
		}

		@Override
		public List<String> getUnits() {
			return Arrays.asList(ResourceService.PERSISTENCE_UNIT);
		}

		@Override
		public Object call(final IBR br) {
			final Resource resource = br.createDAO(ResourceDAO.class).access(request.getUuid());
			if (resource == null) {
				return new ErrorResponse(Errors.MODULE, Errors.UNKNOWN_RESOURCE, request.toString(), null);
			}

			byte[] body;
			try {
				body = Files.readAllBytes(Paths.get(ResourceConfig.instance().getResourceDir(), request.getUuid()));
			} catch (IOException e) {
				return new ErrorResponse(Errors.MODULE, Errors.IO_PROBLEM_WITH_RESOURCE_FILE, request.toString(), e);
			}

			return new GetResourceResponse(resource.getUuid().toString(),
						resource.getName(), resource.getMimeType(), body);
		}
	}

	private static Object handlePutResource(final String bodyText) {
		final PutResourceRequest putRequest = JSONB.instance().unmarshalBody(bodyText, PutResourceRequest.class);
		if (!putRequest.isValid()) {
			return new ErrorResponse(Errors.MODULE, CommonErrors.REQUEST_INVALID, putRequest.toString(), null);
		}
		return BR.execute(logger, new PutResource(putRequest));
	}

	private static final class PutResource implements BRCallable<Object> {
		private final PutResourceRequest request;

		private PutResource(final PutResourceRequest request) {
			this.request = request;
		}

		@Override
		public List<String> getUnits() {
			return Arrays.asList(ResourceService.PERSISTENCE_UNIT);
		}

		@Override
		public Object call(final IBR br) {
			Resource resource = new Resource(request.getName(), request.getMimeType());
			resource = br.createDAO(ResourceDAO.class).merge(resource);
			final String uuid = resource.getUuid().toString();
			try {
				Files.write(Paths.get(ResourceConfig.instance().getResourceDir(), uuid), request.getBody());
			} catch (IOException e) {
				return new ErrorResponse(Errors.MODULE, Errors.IO_PROBLEM_WITH_RESOURCE_FILE, request.toString(), e);
			}
			return new PutResourceResponse(uuid);
		}
	}
}
