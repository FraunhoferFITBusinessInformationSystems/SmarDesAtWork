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
package com.camline.projects.smardes.maindata;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

public class ColumnType {
	private static final int THRESHOLT_VALUE_TYPE_PERCENT = 80;
	private static final int HUNDRED_PERCENT = 100;

	enum HSQLTYPE {
		BOOLEAN,
		INTEGER,
		BIGINT,
		DOUBLE,
		VARCHAR,
		NULL
	}

	private static final int BOOLEAN_VARCHAR_LENGTH = 6;
	private static final int NUMERIC_VARCHAR_LENGTH = 40;

	private ColumnType.HSQLTYPE type;
	private int len;
	private Map<HSQLTYPE, MutableInt> statistics;

	public ColumnType() {
		type = HSQLTYPE.NULL;
		statistics = new EnumMap<>(HSQLTYPE.class);
	}

	public void adjustType(final Serializable value) {
		if (value instanceof String) {
			adjustString((String) value);
			statistics.computeIfAbsent(HSQLTYPE.VARCHAR, hsqltype -> new MutableInt()).increment();
		} else if (value instanceof Boolean) {
			adjustBoolean();
			statistics.computeIfAbsent(HSQLTYPE.BOOLEAN, hsqltype -> new MutableInt()).increment();
		} else if (value instanceof Number) {
			adjustNumber((Number) value);
		}
	}

	public String printValue(final Serializable value) {
		if (value == null) {
			return "null";
		}
		switch (type) {
		case INTEGER:
			return String.valueOf(((Number)value).intValue());
		case BIGINT:
			return String.valueOf(((Number)value).longValue());
		case BOOLEAN:
		case DOUBLE:
			return value.toString();
		default:
			return "'" + value.toString() + "'";
		}
	}

	private void adjustString(final String value) {
		switch (type) {
		case BOOLEAN:
			convertToString(BOOLEAN_VARCHAR_LENGTH);
			break;
		case INTEGER:
		case BIGINT:
		case DOUBLE:
			convertToString(NUMERIC_VARCHAR_LENGTH);
			break;
		default:
			/* VARCHAR, NULL */
			convertToString(value.length());
			break;
		}
	}

	private void adjustBoolean() {
		switch (type) {
		case VARCHAR:
			convertToString(BOOLEAN_VARCHAR_LENGTH);
			break;
		case INTEGER:
		case BIGINT:
		case DOUBLE:
			convertToString(NUMERIC_VARCHAR_LENGTH);
			break;
		default:
			/* BOOLEAN, NULL */
			type = HSQLTYPE.BOOLEAN;
			break;
		}
	}

	private void adjustNumber(final Number value) {
		final int intValue = value.intValue();
		final long longValue = value.longValue();
		final double doubleValue = value.doubleValue();

		final boolean isInteger = intValue == doubleValue;
		final boolean isLong = longValue == doubleValue;
		statistics.computeIfAbsent(
				isInteger || isLong ? HSQLTYPE.INTEGER : HSQLTYPE.DOUBLE,
						hsqltype -> new MutableInt()).increment();

		switch (type) {
		case VARCHAR:
			convertToString(NUMERIC_VARCHAR_LENGTH);
			break;
		case NULL:
		case BOOLEAN:
		case INTEGER:
			if (isInteger) {
				type = HSQLTYPE.INTEGER;
			} else if (isLong) {
				type = HSQLTYPE.BIGINT;
			} else {
				type = HSQLTYPE.DOUBLE;
			}
			break;
		case BIGINT:
			if (!isInteger && !isLong) {
				type = HSQLTYPE.DOUBLE;
			}
			break;
		default:	/* DOUBLE */
			/* Nothing to do */
			break;
		}
	}

	private void convertToString(final int minLength) {
		type = HSQLTYPE.VARCHAR;
		len = Math.max(len, minLength);
	}

	public String toColumnDescription() {
		if (type == HSQLTYPE.VARCHAR) {
			return "varchar(" + len + ")";
		} else if (type == HSQLTYPE.NULL) {
			return "varchar(1)";
		}
		return type.name().toLowerCase();
	}

	public String validate() {
		int totalValues = statistics.values().stream().mapToInt(MutableInt::intValue).sum();
		MutableInt zero = new MutableInt();
		if (type == HSQLTYPE.VARCHAR) {
			int numBooleans = statistics.getOrDefault(HSQLTYPE.BOOLEAN, zero).intValue();
			if (isGreaterNPercent(numBooleans, totalValues, THRESHOLT_VALUE_TYPE_PERCENT)) {
				return "More than 80% of the values are INTEGER but the resulting type is STRING.";
			}
			int numIntegers = statistics.getOrDefault(HSQLTYPE.INTEGER, zero).intValue();
			if (isGreaterNPercent(numIntegers, totalValues, THRESHOLT_VALUE_TYPE_PERCENT)) {
				return "More than 80% of the values are INTEGER but the resulting type is STRING.";
			}
			int numDoubles = statistics.getOrDefault(HSQLTYPE.DOUBLE, zero).intValue();
			if (isGreaterNPercent(numIntegers + numDoubles, totalValues, THRESHOLT_VALUE_TYPE_PERCENT)) {
				return "More than 80% of the values are INTEGER OR DOUBLE but the resulting type is STRING.";
			}
		}
		if (type == HSQLTYPE.DOUBLE) {
			int numIntegers = statistics.getOrDefault(HSQLTYPE.INTEGER, zero).intValue();
			if (isGreaterNPercent(numIntegers, totalValues, THRESHOLT_VALUE_TYPE_PERCENT)) {
				return "More than 80% of the values are INTEGER but the resulting type is DOUBLE.";
			}
		}
		return null;
	}

	private static boolean isGreaterNPercent(int part, int total, int percent) {
		return part * HUNDRED_PERCENT / total > percent;
	}

	@Override
	public String toString() {
		return toColumnDescription();
	}
}
