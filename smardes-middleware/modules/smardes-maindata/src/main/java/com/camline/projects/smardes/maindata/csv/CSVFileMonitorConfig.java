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
package com.camline.projects.smardes.maindata.csv;

import static com.camline.projects.smardes.common.collections.PropertyUtils.getCharProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getIntegerProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getOptionalStringProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getStringProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;

public class CSVFileMonitorConfig {
	private final String name;
	private final URL url;
	private final int interval;
	private final String tableName;
	private final String encoding;
	private final Locale locale;
	private final char delimiter;
	private final String address;

	public CSVFileMonitorConfig(String name, Map<String, String> params, String defaultEncoding, String defaultLocale,
			char defaultDelimiter) {
		this.name = name;
		String value = getStringProperty(params, "url");
		try {
			this.url = new URL(value);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid url " + value, e);
		}
		this.interval = getIntegerProperty(params, "interval");
		this.tableName = getStringProperty(params, "table");
		this.encoding = getStringProperty(params, "encoding", defaultEncoding);
		this.locale = LocaleUtils.toLocale(getStringProperty(params, "locale", defaultLocale));
		this.delimiter = getCharProperty(params, "delimiter", defaultDelimiter);
		this.address = getOptionalStringProperty(params, "address");
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
	}

	public int getInterval() {
		return interval;
	}

	public String getTableName() {
		return tableName;
	}

	public Charset getCharset() {
		return Charset.forName(encoding);
	}

	public Locale getLocale() {
		return locale;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public String getAddress() {
		return address;
	}
}
