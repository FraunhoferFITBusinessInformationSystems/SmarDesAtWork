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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.br.IBR;

public class XLSToDB extends AbstractFileToDB {
	private static final Logger logger = LoggerFactory.getLogger(XLSToDB.class);

	public String execute(final File xlsFile, final Sheet sheet) {
		logger.info("Now importing sheet {}", sheet.getSheetName());

		final Iterator<Row> it = sheet.iterator();
		if (!it.hasNext()) {
			return null;
		}

		final Map<Integer, String> columnNames = new TreeMap<>();
		final Map<String, ColumnType> columnTypes = new HashMap<>();

		final Row firstRow = it.next();
		firstRow.cellIterator().forEachRemaining(cell -> {
			final String columnName = cell.getStringCellValue();
			final Integer columnIndex = Integer.valueOf(cell.getColumnIndex());
			columnNames.put(columnIndex, columnName);
			columnTypes.put(columnName, new ColumnType());
		});

		final List<Map<String, Serializable>> sheetValues = new ArrayList<>();
		it.forEachRemaining(row -> {
			final Map<String, Serializable> rowValues = new HashMap<>();
			for (Cell cell : row) {
				final Integer columnIndex = Integer.valueOf(cell.getColumnIndex());
				final String columnName = columnNames.get(columnIndex);
				if (columnName != null) {
					final Serializable value = getCellValue(cell);
					columnTypes.get(columnName).adjustType(value);
					rowValues.put(columnName, value);
				}
			}
			if (isRowEmpty(rowValues)) {
				sheetValues.add(rowValues);
			}
		});

		checkTypeConversion(columnTypes);

		BR.execute(logger, new AbstractDatabaseImporter(sheet.getSheetName(), "XLS", columnNames.values(), columnTypes,
				sheetValues) {
			@Override
			protected void addContext(final IBR br) {
				br.addContext("File", xlsFile.getName());
				br.addContext("Sheet", sheet.getSheetName());
			}
		});

		return sheet.getSheetName();
	}

	private static Serializable getCellValue(final Cell cell) {
		switch (cell.getCellType()) {
		case NUMERIC:
			return Double.valueOf(cell.getNumericCellValue());
		case STRING:
			return cell.getStringCellValue();
		case BLANK:
			return null;
		case BOOLEAN:
			return Boolean.valueOf(cell.getBooleanCellValue());
		default:
			logger.warn("Unsupported cell type {}. Assume blank...", cell.getCellType());
			return null;
		}
	}
}
