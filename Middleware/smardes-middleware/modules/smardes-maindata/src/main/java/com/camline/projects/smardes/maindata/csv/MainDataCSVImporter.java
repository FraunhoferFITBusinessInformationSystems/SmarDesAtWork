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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.common.io.DirectoryMonitor;
import com.camline.projects.smardes.jsonapi.SmarDesException;
import com.camline.projects.smardes.maindata.MainDataConfig;
import com.camline.projects.smardes.maindata.TableDropper;

public class MainDataCSVImporter extends DirectoryMonitor<String> {
	private static final Logger logger = LoggerFactory.getLogger(MainDataCSVImporter.class);

	public MainDataCSVImporter(final String directory) {
		super(directory);
	}

	@Override
	public boolean accept(final File dir, final String name) {
		return name.endsWith(".csv");
	}

	@Override
	protected String processFile(final File csvFile, final String oldTableName) {
		final String tableName = StringUtils.substringBeforeLast(csvFile.getName(), ".");

		// There is nothing to check against oldTableName since both derive from file name

		try (Reader reader = Files.newBufferedReader(csvFile.toPath(), MainDataConfig.instance().getCSVCharset())) {
			CSVToDB csvToDB = new CSVToDB();
			csvToDB.execute(reader, "CSV", csvFile.getName(), tableName, MainDataConfig.instance().getCSVLocale(),
					MainDataConfig.instance().getCSVDelimiter());
		} catch (IOException e) {
			throw new SmarDesException("Problem with CSV file " + csvFile, e);
		}

		return tableName;
	}

	@Override
	protected void processDeletedFile(final File csvFile, final String oldResult) {
		BR.execute(logger, new TableDropper(oldResult) {
			@Override
			protected void addContext(final IBR br) {
				br.addContext("File", csvFile.getName());
			}
		});
	}
}
