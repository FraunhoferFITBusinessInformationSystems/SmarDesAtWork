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

import java.time.ZonedDateTime;

import javax.json.bind.adapter.JsonbAdapter;

/**
 * We want to always send date strings that conform to ISO8601.
 * ZonedDateTime adds the time zone which is not compliant.
 *
 * @author matze
 */
public class ZonedDateTimeAdapter implements JsonbAdapter<ZonedDateTime, String> {
	@Override
	public String adaptToJson(ZonedDateTime obj) throws Exception {
		return obj.toOffsetDateTime().toString();
	}

	@Override
	public ZonedDateTime adaptFromJson(String text) throws Exception {
		return ZonedDateTime.parse(text);
	}
}
