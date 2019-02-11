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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.maindata.AbstractDatabaseImporter;
import com.camline.projects.smardes.maindata.AbstractFileToDB;
import com.camline.projects.smardes.maindata.ColumnType;

public class CSVToDB extends AbstractFileToDB {
	private static final Logger logger = LoggerFactory.getLogger(CSVToDB.class);

	public void execute(final Reader reader, final String importId, final String resourceName, final String tableName,
			final Locale locale, final char delimiter) throws IOException {
		NumberFormat numberFormat = NumberFormat.getInstance(locale);
		numberFormat.setGroupingUsed(false);

		try (CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()
				.withDelimiter(delimiter))) {
			final Map<String, Integer> headerMap = csvParser.getHeaderMap();
			final Map<String, ColumnType> columnTypes = headerMap.keySet().stream()
					.collect(Collectors.toMap(Function.identity(), columnName -> new ColumnType()));

			final List<Map<String, Serializable>> sheetValues = new ArrayList<>();
			for (final CSVRecord csvRecord : csvParser) {
				final Map<String, Serializable> rowValues = new HashMap<>();
				for (final Entry<String, String> entry : csvRecord.toMap().entrySet()) {
					final Serializable value = guessBestType(entry.getValue(), numberFormat);
					columnTypes.get(entry.getKey()).adjustType(value);
					rowValues.put(entry.getKey(), value);
				}
				if (isRowEmpty(rowValues)) {
					sheetValues.add(rowValues);
				}
			}

			checkTypeConversion(columnTypes);

			BR.execute(logger, new AbstractDatabaseImporter(tableName, importId, headerMap.keySet(), columnTypes, sheetValues) {
				@Override
				protected void addContext(final IBR br) {
					br.addContext("File", resourceName);
				}
			});
		}

	}

	private static Serializable guessBestType(final String strValue, final NumberFormat numberFormat) {
		if (strValue == null) {
			return null;
		}

		try {
			Integer value = Integer.valueOf(strValue);
			if (strValue.equals(value.toString())) {
				return value;
			}
			return strValue;
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			// continue
		}

		try {
			final ParsePosition parsePosition = new ParsePosition(0);
			final Number number = numberFormat.parse(strValue, parsePosition);

			if (parsePosition.getIndex() != strValue.length()) {
				throw new ParseException("Invalid input", parsePosition.getIndex());
			}
			return number;
		} catch (@SuppressWarnings("unused") final ParseException e) {
			return strValue;
		}
	}

}
