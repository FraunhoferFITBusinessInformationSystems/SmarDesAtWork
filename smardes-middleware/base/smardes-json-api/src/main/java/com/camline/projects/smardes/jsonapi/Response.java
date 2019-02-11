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
package com.camline.projects.smardes.jsonapi;

import java.util.Collection;

public class Response {
	private ErrorResponse error;
	private Object responseObject;
	private Collection<Object> responseCollection;

	public Response() {
		// default constructor needed by JSON-B
	}

	@SuppressWarnings("unchecked")
	public Response(final Object responseObject) {
		if (responseObject instanceof ErrorResponse) {
			this.error = (ErrorResponse) responseObject;
		} else if (responseObject instanceof Collection) {
			this.responseCollection = (Collection<Object>) responseObject;
		} else {
			this.responseObject = responseObject;
		}
	}

	public ErrorResponse getError() {
		return error;
	}

	public void setError(final ErrorResponse error) {
		this.error = error;
	}

	public Object getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(final Object responseObject) {
		this.responseObject = responseObject;
	}

	public Collection<Object> getResponseCollection() {
		return responseCollection;
	}

	public void setResponseCollection(final Collection<Object> responseCollection) {
		this.responseCollection = responseCollection;
	}
}
