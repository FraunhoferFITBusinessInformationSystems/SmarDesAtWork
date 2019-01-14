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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder({"module", "errorCode", "errorText"})
public class ErrorResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private String module;
	private String errorCode;
	private String errorText;
	private String callStack;

	public ErrorResponse() {
		// default constructor needed by JSON-B
	}

	public ErrorResponse(final String module, final String errorCode) {
		this(module, errorCode, null, null);
	}

	public ErrorResponse(final String module, final Enum<?> errorCodeEnum) {
		this(module, errorCodeEnum, null, null);
	}

	public ErrorResponse(final String module, final Enum<?> errorCodeEnum, final Exception e) {
		this(module, errorCodeEnum, null, e);
	}

	public ErrorResponse(final String module, final Enum<?> errorCodeEnum, final String errorText, final Exception e) {
		this(module, errorCodeEnum.name(), errorText, e);
	}

	public ErrorResponse(final String module, final String errorCode, final String errorText, final Exception e) {
		this.module = module;
		this.errorCode = errorCode;
		this.errorText = errorText;
		if (e != null) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			this.callStack = sw.toString();
		}
	}

	public String getModule() {
		return module;
	}

	public void setModule(final String module) {
		this.module = module;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(final String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(final String errorText) {
		this.errorText = errorText;
	}

	public String getCallStack() {
		return callStack;
	}

	public void setCallStack(final String callStack) {
		this.callStack = callStack;
	}

	@Override
	public String toString() {
		return "ErrorResponse [module=" + module + ", errorCode=" + errorCode + ", errorText=" + errorText + "]";
	}
}
