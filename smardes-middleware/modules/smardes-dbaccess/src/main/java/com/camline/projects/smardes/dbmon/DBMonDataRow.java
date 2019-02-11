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
package com.camline.projects.smardes.dbmon;

import java.util.Map;

import com.camline.projects.smardes.jsonapi.SmarDesException;

public class DBMonDataRow {
	private final Object lastObject1;
	private final Object lastObject2;
	private final Object lastObject3;
	private final Map<String, Object> row;

	public DBMonDataRow(Object lastObject1, Object lastObject2, Object lastObject3, Map<String, Object> row) {
		if (lastObject1 == null) {
			throw new SmarDesException("At least the first trigger value must be set");
		}
		this.lastObject1 = lastObject1;
		this.lastObject2 = lastObject2;
		this.lastObject3 = lastObject3;
		this.row = row;
	}

	public Object getLastObject1() {
		return lastObject1;
	}

	public Object getLastObject2() {
		return lastObject2;
	}

	public Object getLastObject3() {
		return lastObject3;
	}

	public Map<String, Object> getRow() {
		return row;
	}
}
