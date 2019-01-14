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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.dq.DataQueryBR;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public class DBMonDataQuery extends DataQueryBR {
	private static final String TRIGGER_COLUMN_NAME1 = "dbmon_trigger_col1__";
	private static final String TRIGGER_COLUMN_NAME2 = "dbmon_trigger_col2__";
	private static final String TRIGGER_COLUMN_NAME3 = "dbmon_trigger_col3__";

	private final MonitorConfig monitorConfig;
	private final boolean initial;
	private final String monitorColumn1;
	private final String monitorColumn2;
	private final String monitorColumn3;

	/**
	 * Constructor for initial query
	 *
	 * @param monitorConfig DB monitor configuration
	 */
	public DBMonDataQuery(final MonitorConfig monitorConfig) {
		this(monitorConfig, true, null);
	}

	/**
	 * Constructor for standard query
	 * @param monitorConfig DB monitor configuration
	 * @param bindParametersNamed named bind parameters
	 */
	public DBMonDataQuery(final MonitorConfig monitorConfig, final List<Object> bindParameters) {
		this(monitorConfig, false, createNamedBindParameters(bindParameters));
	}

	private DBMonDataQuery(final MonitorConfig monitorConfig, final boolean initial,
			final Map<String, Object> bindParametersNamed) {
		super(initial ? monitorConfig.getInitQuery() : monitorConfig.getQuery(),
				initial ? 1 : monitorConfig.getMaxItems(), null, bindParametersNamed);
		this.monitorConfig = monitorConfig;
		this.initial = initial;
		this.monitorColumn1 = surroundParenthesis(monitorConfig.getColumn1());
		this.monitorColumn2 = surroundParenthesis(monitorConfig.getColumn2());
		this.monitorColumn3 = surroundParenthesis(monitorConfig.getColumn3());
	}

	private static String surroundParenthesis(String arg) {
		if (arg == null) {
			return null;
		}
		return "(" + arg + ")";
	}

	private static Map<String, Object> createNamedBindParameters(List<Object> bindParameters) {
		if (bindParameters == null) {
			return null;
		}

		if (bindParameters.isEmpty()) {
			throw new SmarDesException("Empty bind parameter list");
		}

		Map<String, Object> bindParametersNamed = new HashMap<>();
		bindParametersNamed.put(TRIGGER_COLUMN_NAME1, bindParameters.get(0));
		if (bindParameters.size() > 1) {
			bindParametersNamed.put(TRIGGER_COLUMN_NAME2, bindParameters.get(1));
		}
		if (bindParameters.size() > 2) {
			bindParametersNamed.put(TRIGGER_COLUMN_NAME3, bindParameters.get(2));
		}
		return bindParametersNamed;
	}

	@Override
	public List<Map<String, Object>> call(final IBR br) {
		if (initial) {
			br.addContext("initialQuery", "true");
		}
		return super.call(br);
	}

	public List<DBMonDataRow> convert(Collection<Map<String, Object>> rows) {
		return rows.stream()
				.map(row -> new DBMonDataRow(
						getUniqueEntry(row, TRIGGER_COLUMN_NAME1, monitorConfig.getColumn1()),
						getUniqueEntry(row, TRIGGER_COLUMN_NAME2, monitorConfig.getColumn2()),
						getUniqueEntry(row, TRIGGER_COLUMN_NAME3, monitorConfig.getColumn3()), row))
				.collect(Collectors.toList());
	}

	private static Object getUniqueEntry(final Map<String, Object> row, final String triggerColumnName,
			final String monitorColumnName) {
		final List<Entry<String, Object>> entryList = row.entrySet().stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(triggerColumnName))
				.collect(Collectors.toList());

		switch (entryList.size()) {
		case 0:
			if (monitorColumnName != null) {
				throw new SmarDesException(String.format("Cannot find column %s/%s in monitored row %s",
						monitorColumnName, triggerColumnName, row));
			}
			return null;
		case 1:
			return entryList.get(0).getValue();
		default:
			throw new SmarDesException(String.format("Ambiguous or no entries for column %s/%s in monitored row %s",
					monitorColumnName, triggerColumnName, row));
		}
	}

	@Override
	protected String createStatement() {
		final StringBuilder stmt = new StringBuilder();
		stmt.append("SELECT " + monitorColumn1 + " AS " + TRIGGER_COLUMN_NAME1 + ", ");
		if (monitorColumn2 != null) {
			stmt.append(monitorColumn2 + " AS " + TRIGGER_COLUMN_NAME2 + ", ");
		}
		if (monitorColumn3 != null) {
			stmt.append(monitorColumn3 + " AS " + TRIGGER_COLUMN_NAME3 + ", ");
		}
		stmt.append("s.* FROM (" + super.createStatement() + ") s ");

		if (initial) {
			appendOrderBy(stmt, false, monitorColumn1, monitorColumn2, monitorColumn3);
		} else {
			appendStandardPart(stmt);
			appendOrderBy(stmt, true, monitorColumn1, monitorColumn2, monitorColumn3);
		}

		return stmt.toString();
	}

	private void appendStandardPart(final StringBuilder stmt) {
		if (bindParametersNamed != null) {
			stmt.append("WHERE " + gt(monitorColumn1, TRIGGER_COLUMN_NAME1));
			if (monitorColumn2 != null) {
				stmt.append(" OR (" + and(eq(monitorColumn1, TRIGGER_COLUMN_NAME1),
						gt(monitorColumn2, TRIGGER_COLUMN_NAME2)) + ")");
			}
			if (monitorColumn3 != null) {
				stmt.append(" OR (" + and(eq(monitorColumn1, TRIGGER_COLUMN_NAME1),
						eq(monitorColumn2, TRIGGER_COLUMN_NAME2),
						gt(monitorColumn3, TRIGGER_COLUMN_NAME3)) + ")");
			}
		}
	}

	private static String and(String... constraint) {
		if (constraint.length == 0) {
			return StringUtils.EMPTY;
		}

		StringBuilder buf = new StringBuilder(constraint[0]);
		for (int i = 1; i < constraint.length; i++) {
			buf.append(" AND ").append(constraint[i]);
		}
		return buf.toString();
	}

	private static String eq(String columnName, String bindVarName) {
		return columnName + " = :" + bindVarName;
	}

	private static String gt(String columnName, String bindVarName) {
		return columnName + " > :" + bindVarName;
	}

	private static void appendOrderBy(StringBuilder stmt, boolean asc, String... columns) {
		boolean first = true;

		for (String column : columns) {
			if (column == null) {
				continue;
			}
			if (first) {
				stmt.append(" ORDER BY ");
				first = false;
			} else {
				stmt.append(", ");
			}
			stmt.append(column);
			if (!asc) {
				stmt.append(" DESC");
			}
		}
	}
}
