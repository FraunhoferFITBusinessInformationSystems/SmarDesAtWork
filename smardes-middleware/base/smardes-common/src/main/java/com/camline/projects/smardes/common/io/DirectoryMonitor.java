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
package com.camline.projects.smardes.common.io;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DirectoryMonitor<T> extends AbstractDirectoryMonitor {
	private static final Logger logger = LoggerFactory.getLogger(DirectoryMonitor.class);

	private final Map<File, Long> modificationTimes;
	private final Map<File, T> processedFileResults;

	public DirectoryMonitor(final String directory) {
		this(new File(directory));
	}

	public DirectoryMonitor(final File directory) {
		super(directory);
		this.modificationTimes = new HashMap<>();
		this.processedFileResults = new HashMap<>();
	}

	protected abstract T processFile(File file, T oldResult);

	protected Map<File, T> getProcessedFileResults() {
		return processedFileResults;
	}

	/**
	 * React on file deletion.
	 *
	 * @param file file that was deleted
	 * @param oldResult last processFile result with this file
	 * @throws IOException on any I/O error
	 */
	protected void processDeletedFile(final File file, final T oldResult) {
		throw new UnsupportedOperationException("This monitor is not prepared for file deletion.");
	}

	@Override
	public void run() {
		if (!directory.exists()) {
			logger.warn("Directory {} does not exist, skipping...", directory);
			return;
		}

		final Set<File> newProcessedFiles = Stream.of(directory.listFiles(this)).sorted().collect(Collectors.toSet());
		boolean changed = false;

		/*
		 * First work on deleted files...
		 */
		final Set<File> deletedFiles = new HashSet<>(processedFileResults.keySet());
		deletedFiles.removeAll(newProcessedFiles);
		for (final File deletedFile : deletedFiles) {
			changed |= doProcessDeletedFile(deletedFile);
		}

		for (final File file : newProcessedFiles) {
			changed |= doProcessFile(file);
		}

		logSummary(changed);
	}

	private boolean doProcessDeletedFile(final File deletedFile) {
		boolean changed = false;
		modificationTimes.remove(deletedFile);
		final T oldResult = processedFileResults.remove(deletedFile);

		try {
			processDeletedFile(deletedFile, oldResult);
			changed = true;
		} catch (final RuntimeException e) {
			logger.warn("Error in replicating remove of maindata file {}", deletedFile.getName(), e);
		}
		return changed;
	}

	private boolean doProcessFile(final File file) {
		final Long previousModified = modificationTimes.get(file);
		final long lastModifiedFile = file.lastModified();
		if (previousModified != null && previousModified.longValue() == lastModifiedFile) {
			return false;
		}

		boolean changed = false;
		try {
			T oldResult = processedFileResults.get(file);
			if (oldResult != null) {
				logger.info("File {} has been changed ({} vs. {}). Processing...", file.getName(),
						previousModified != null ? Instant.ofEpochMilli(previousModified.longValue()) : null,
						Instant.ofEpochMilli(lastModifiedFile));
			} else {
				logger.info("Processing new file {}...", file.getName());
			}

			final T result = processFile(file, oldResult);
			changed = true;

			modificationTimes.put(file, Long.valueOf(lastModifiedFile));
			processedFileResults.put(file, result);
		} catch (final RuntimeException e) {
			logger.warn("Error in file " + file.getName() + ", no file processing...", e);
		}
		return changed;
	}

	protected void logSummary(boolean changed) {
		if (changed) {
			String summary = processedFileResults.entrySet().stream()
					.map(entry -> entry.getKey() + ": " + entry.getValue())
					.collect(Collectors.joining("\n\t", "\n\t", ""));
			logger.info("Summary:{}", summary);
		}
	}
}
