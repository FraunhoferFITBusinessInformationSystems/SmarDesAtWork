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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import com.camline.projects.smardes.common.br.BRVoidCallable;
import com.camline.projects.smardes.common.br.IBR;

public abstract class AbstractDatabaseImporter implements BRVoidCallable {
	private final String tableName;
	private final String workTableName;
	private final String dropTableName;
	private final Collection<String> columnNames;
	private final Map<String, ColumnType> columnTypes;
	private final List<Map<String, Serializable>> sheetValues;

	public AbstractDatabaseImporter(final String tableName, final String tempTableId,
			final Collection<String> columnNames, final Map<String, ColumnType> columnTypes,
			final List<Map<String, Serializable>> sheetValues) {
		this.tableName = tableName;
		this.workTableName = "MD_WORK_" + tempTableId;
		this.dropTableName = "MD_DROP_" + tempTableId;
		this.columnNames = columnNames;
		this.columnTypes = columnTypes;
		this.sheetValues = sheetValues;
	}

	@Override
	public String getName() {
		return "ImportSheetData";
	}


	@Override
	public List<String> getUnits() {
		return Arrays.asList(MainDataService.PERSISTENCE_UNIT);
	}

	@Override
	public void call(final IBR br) {
		br.addContext("Table", tableName);
		br.addContext("Columns", columnNames.size());
		br.addContext("Rows", sheetValues.size());
		addContext(br);
		createWorkTable(br);
		insertRows(br);
		switchTables(br);
	}

	protected abstract void addContext(final IBR br);

	private static String escapeSQL(String columnName) {
		return StringUtils.replaceChars(columnName, "- .", "___");
	}

	private void createWorkTable(final IBR br) {
		dropTable(br, workTableName);

		final StringBuilder stmt = new StringBuilder("create table " + workTableName + " (");
		stmt.append(columnNames.stream()
				.map(columnName -> "\n\t" + escapeSQL(columnName) + " " + columnTypes.get(columnName).toColumnDescription())
				.collect(Collectors.joining(",")));
		stmt.append("\n);\n");

		br.getEntityManager().createNativeQuery(stmt.toString()).executeUpdate();
	}

	private void insertRows(final IBR br) {
		final StringBuilder start = new StringBuilder();
		start.append("insert into " + workTableName + " (");
		boolean first = true;
		for (final String columnName : columnNames) {
			if (first) {
				first = false;
			} else {
				start.append(", ");
			}
			start.append(escapeSQL(columnName));
		}
		start.append(") values (");

		for (final Map<String, Serializable> row : sheetValues) {
			final StringBuilder stmt = new StringBuilder(start);
			first = true;
			for (final String columnName : columnNames) {
				if (first) {
					first = false;
				} else {
					stmt.append(", ");
				}
				final ColumnType columnType = columnTypes.get(columnName);
				stmt.append(columnType.printValue(row.get(columnName)));
			}
			stmt.append(");\n");
			br.getEntityManager().createNativeQuery(stmt.toString()).executeUpdate();
		}
	}

	protected void switchTables(final IBR br) {
		dropTable(br, dropTableName);
		if (tableExists(br, tableName)) {
			renameTable(br, tableName, dropTableName);
		}
		renameTable(br, workTableName, tableName);
		dropTable(br, dropTableName);
	}

	private static boolean tableExists(final IBR br, final String myTableName) {
		final MutableBoolean tableExists = new MutableBoolean();
		br.getEntityManager().unwrap(Session.class).doWork(new Work() {
			@Override
			public void execute(final Connection connection) throws SQLException {
				try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
					while (rs.next()) {
						if (myTableName.equalsIgnoreCase(rs.getString(3))) {
							tableExists.setTrue();
							return;
						}
					}
				}
				tableExists.setFalse();
			}
		});
		return tableExists.booleanValue();
	}

	private static int dropTable(final IBR br, final String tableName) {
		final String stmt = String.format("drop table %s if exists;", tableName);
		return br.getEntityManager().createNativeQuery(stmt).executeUpdate();
	}

	private static int renameTable(final IBR br, final String oldTableName, final String newTableName) {
		final String stmt = String.format("alter table %s rename to %s;", oldTableName, newTableName);
		return br.getEntityManager().createNativeQuery(stmt).executeUpdate();
	}
}
