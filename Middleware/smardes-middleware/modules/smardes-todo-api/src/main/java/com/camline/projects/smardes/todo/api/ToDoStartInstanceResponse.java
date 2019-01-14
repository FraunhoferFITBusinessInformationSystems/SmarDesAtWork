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
package com.camline.projects.smardes.todo.api;

import java.util.UUID;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

import com.camline.projects.smardes.todo.api.dto.ToDoHeaderDTO;

public class ToDoStartInstanceResponse {
	private UUID instanceId;
	private ToDoHeaderDTO header;


	@JsonbCreator
	public ToDoStartInstanceResponse(
			@JsonbProperty("header") final ToDoHeaderDTO header,
			@JsonbProperty("instanceId") final UUID instanceId) {
		this.header = header;
		this.instanceId = instanceId;
	}

	public ToDoHeaderDTO getHeader() {
		return header;
	}

	public void setHeader(ToDoHeaderDTO header) {
		this.header = header;
	}

	public UUID getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(UUID instanceId) {
		this.instanceId = instanceId;
	}

	@JsonbTransient
	public boolean isValid() {
		return instanceId != null;
	}

	@Override
	public String toString() {
		return "ToDoStartInstanceResponse [instanceId=" + instanceId + "]";
	}
}
