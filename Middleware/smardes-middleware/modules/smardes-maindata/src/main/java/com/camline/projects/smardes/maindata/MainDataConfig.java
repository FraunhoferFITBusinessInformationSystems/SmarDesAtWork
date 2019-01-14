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

import static com.camline.projects.smardes.common.collections.PropertyUtils.extractMultiProperties;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getCharProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.getStringProperty;
import static com.camline.projects.smardes.common.collections.PropertyUtils.groupProperties;
import static com.camline.projects.smardes.common.collections.PropertyUtils.loadSmardesConfiguration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.maindata.csv.CSVFileMonitorConfig;

public final class MainDataConfig {
	private static final Logger logger = LoggerFactory.getLogger(MainDataConfig.class);

	private static final String PROP_XLSX_FOLDER = "xlsx.folder";
	private static final String PROP_RAW_FOLDER = "raw.folder";
	private static final String PROP_CSV_FOLDER = "csv.folder";
	private static final String PROP_CSV_DELIMITER = "csv.delimiter";
	private static final String PROP_CSV_ENCODING = "csv.encoding";
	private static final String PROP_CSV_LOCALE = "csv.locale";
	private static final String PROP_MAINDATA_ADDRESS = "maindata.address";

	private static final MainDataConfig instance = new MainDataConfig();

	private final Collection<String> xlsxFolders;
	private final Collection<String> csvFolders;

	private final String rawFolder;

	private final char csvDelimiter;
	private final String csvEncoding;
	private final Locale csvLocale;

	private final String mainDataAddress;

	private final List<CSVFileMonitorConfig> csvFileMonitorConfigs;

	private MainDataConfig() {
		Properties properties;
		try {
			properties = loadSmardesConfiguration("maindata.properties");
		} catch (final IOException e) {
			logger.error("Cannot read maindata.properties. Module not configured", e);
			properties = new Properties();
		}

		xlsxFolders = extractMultiProperties(properties, PROP_XLSX_FOLDER).values();
		rawFolder = getStringProperty(properties, PROP_RAW_FOLDER);
		csvFolders = extractMultiProperties(properties, PROP_CSV_FOLDER).values();
		csvDelimiter = getCharProperty(properties, PROP_CSV_DELIMITER);
		csvEncoding = getStringProperty(properties, PROP_CSV_ENCODING);
		String localeString = getStringProperty(properties, PROP_CSV_LOCALE);
		csvLocale = LocaleUtils.toLocale(getStringProperty(properties, PROP_CSV_LOCALE));
		mainDataAddress = getStringProperty(properties, PROP_MAINDATA_ADDRESS);

		csvFileMonitorConfigs = groupProperties(properties, "csv.file").entrySet().stream()
				.map(entry -> new CSVFileMonitorConfig(entry.getKey(), entry.getValue(), csvEncoding, localeString, csvDelimiter))
				.collect(Collectors.toList());
	}

	public static MainDataConfig instance() {
		return instance;
	}

	public Collection<String> getXLSXFolders() {
		return xlsxFolders;
	}

	public Collection<String> getCSVFolders() {
		return csvFolders;
	}

	public String getRawFolder() {
		return rawFolder;
	}

	public char getCSVDelimiter() {
		return csvDelimiter;
	}

	public Charset getCSVCharset() {
		return Charset.forName(csvEncoding);
	}

	public Locale getCSVLocale() {
		return csvLocale;
	}

	public String getMainDataAddress() {
		return mainDataAddress;
	}

	public List<CSVFileMonitorConfig> getCSVFileMonitorConfigs() {
		return csvFileMonitorConfigs;
	}
}
