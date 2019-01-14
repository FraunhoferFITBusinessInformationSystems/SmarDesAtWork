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
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.common.io.DirectoryMonitor;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public class MainDataXLSImporter extends DirectoryMonitor<Set<String>> {
	private static final Logger logger = LoggerFactory.getLogger(MainDataXLSImporter.class);

	public MainDataXLSImporter(final String directory) {
		super(directory);
	}

	@Override
	public boolean accept(final File dir, final String name) {
		return name.endsWith(".xlsx");
	}

	@Override
	protected Set<String> processFile(final File xlsxFile, final Set<String> oldTableNames) {
		final Set<String> existingTableNames = getProcessedFileResults().entrySet().stream()
				.filter(entry -> !entry.getKey().equals(xlsxFile)).flatMap(entry -> entry.getValue().stream())
				.collect(Collectors.toSet());
		final Set<String> newTableNames = new HashSet<>();
		final Set<String> refusedTableNames = new HashSet<>();

		final XLSToDB xlsToDB = new XLSToDB();
		try (XSSFWorkbook workbook = XSSFWorkbookFactory.createWorkbook(xlsxFile, true)) {
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				final XSSFSheet sheet = workbook.getSheetAt(i);
				final SheetVisibility sheetVisibility = workbook.getSheetVisibility(i);
				if (sheetVisibility != SheetVisibility.VISIBLE) {
					logger.warn("Skipping sheet {} in file {} since its visibility state is {}.", sheet.getSheetName(),
							xlsxFile, sheetVisibility);
					refusedTableNames.add(sheet.getSheetName());
					continue;
				}
				if (existingTableNames.contains(sheet.getSheetName())) {
					logger.warn("Table {} is already existing. Importing of this sheet in file {} skipped!",
							sheet.getSheetName(), xlsxFile);
					refusedTableNames.add(sheet.getSheetName());
					continue;
				}
				final String tableName = xlsToDB.execute(xlsxFile, sheet);
				if (tableName != null) {
					newTableNames.add(tableName);
				}
			}
		} catch (InvalidFormatException | IOException e) {
			throw new SmarDesException("Problem opening workbook " + xlsxFile, e);
		}

		Set<String> deletedTableNames;
		if (oldTableNames != null) {
			deletedTableNames = new HashSet<>(oldTableNames);
			deletedTableNames.removeAll(newTableNames);
			processDeletedFile(xlsxFile, deletedTableNames);
		} else {
			deletedTableNames = Collections.emptySet();
		}

		logger.info("XLS Import {}: added/updated {}, deleted {}, skipped {}", xlsxFile.getName(), newTableNames,
				deletedTableNames, refusedTableNames);
		return newTableNames;
	}

	@Override
	protected void processDeletedFile(final File xlsFile, final Set<String> deleteTables) {
		for (final String deletedTable : deleteTables) {
			try {
				BR.execute(logger, new TableDropper(deletedTable) {
					@Override
					protected void addContext(final IBR br) {
						br.addContext("File", xlsFile.getName());
					}
				});
			} catch (final RuntimeException e) {
				logger.warn("Error in replicating remove of maindata file " + xlsFile.getName() + " and table "
						+ deletedTable, e);
			}
		}

		if (!deleteTables.isEmpty()) {
			logger.info("XLS Import {}: deleted {}", xlsFile.getName(), deleteTables);
		}
	}
}
