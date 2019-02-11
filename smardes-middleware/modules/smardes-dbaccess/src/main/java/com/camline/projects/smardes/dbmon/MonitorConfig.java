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

import static com.camline.projects.smardes.common.collections.PropertyUtils.getBooleanProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getDoubleProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getIntegerProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getOptionalStringProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getStringProperty;

import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;

import com.camline.projects.smardes.jsonapi.SmarDesException;

public class MonitorConfig {
	private static final String PROP_DBMON_ACTIVE = "active";
	private static final String PROP_DBMON_INITQUERY = "initquery";
	private static final String PROP_DBMON_QUERY = "query";
	private static final String PROP_DBMON_COLUMN1 = "column1";
	private static final String PROP_DBMON_COLUMN2 = "column2";
	private static final String PROP_DBMON_COLUMN3 = "column3";
	private static final String PROP_DBMON_INTERVAL = "interval";
	private static final String PROP_DBMON_MAXITEMS = "maxitems";
	private static final String PROP_DBMON_ADDRESS = "address";

	private final String name;
	private final boolean active;
	private final String initquery;
	private final String query;
	private final String column1;
	private final String column2;
	private final String column3;
	private final long interval;
	private final int maxItems;
	private final String address;

	public MonitorConfig(final String name, final Map<String, String> properties) {
		super();
		this.name = name;
		this.active = getBooleanProperty(properties, PROP_DBMON_ACTIVE);
		this.initquery = getOptionalStringProperty(properties, PROP_DBMON_INITQUERY);
		this.query = getStringProperty(properties, PROP_DBMON_QUERY);
		this.column1 = getStringProperty(properties, PROP_DBMON_COLUMN1);
		this.column2 = getOptionalStringProperty(properties,PROP_DBMON_COLUMN2);
		this.column3 = getOptionalStringProperty(properties, PROP_DBMON_COLUMN3);
		this.interval = (long) (getDoubleProperty(properties, PROP_DBMON_INTERVAL) * DateUtils.MILLIS_PER_SECOND);
		this.maxItems = getIntegerProperty(properties, PROP_DBMON_MAXITEMS);
		this.address = getStringProperty(properties, PROP_DBMON_ADDRESS);

		if (interval <= 0 || maxItems <= 0) {
			throw new SmarDesException("Base configuration is wrong: " + this);
		}
		if (column2 == null && column3 != null) {
			throw new SmarDesException("Column configuration is wrong: " + this);
		}
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}

	public String getInitQuery() {
		return initquery;
	}

	public String getQuery() {
		return query;
	}

	public String getColumn1() {
		return column1;
	}

	public String getColumn2() {
		return column2;
	}

	public String getColumn3() {
		return column3;
	}

	public long getInterval() {
		return interval;
	}

	public int getMaxItems() {
		return maxItems;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return "MonitorConfig [name=" + name + ", active=" + active + ", initquery=" + initquery + ", query=" + query
				+ ", column1=" + column1 + ", column2=" + column2 + ", column3=" + column3 + ", interval=" + interval
				+ ", maxItems=" + maxItems + ", address=" + address + "]";
	}
}
