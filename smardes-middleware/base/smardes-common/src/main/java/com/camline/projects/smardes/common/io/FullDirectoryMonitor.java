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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FullDirectoryMonitor extends AbstractDirectoryMonitor {
	private static final Logger logger = LoggerFactory.getLogger(FullDirectoryMonitor.class);

	private long latestModificationTime;
	private Set<File> processedFiles;

	public FullDirectoryMonitor(final String directory) {
		this(new File(directory));
	}

	public FullDirectoryMonitor(final File directory) {
		super(directory);
		this.processedFiles = Collections.emptySet();
	}

	protected abstract void processDirectory()
			throws IOException;

	protected Set<File> getProcessedFileResults() {
		return processedFiles;
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
		final Set<File> deletedFiles = new HashSet<>(processedFiles);
		deletedFiles.removeAll(newProcessedFiles);

		long newLatestModificationTime = deletedFiles.isEmpty() ? 0 : System.currentTimeMillis();

		if (deletedFiles.isEmpty()) {
			for (final File file : newProcessedFiles) {
				newLatestModificationTime = Math.max(newLatestModificationTime, file.lastModified());
			}
		}

		if (newLatestModificationTime > latestModificationTime) {
			try {
				logger.info("Directory {} has been changed ({} vs. {}). Processing...", directory.getName(),
						Instant.ofEpochMilli(latestModificationTime), Instant.ofEpochMilli(newLatestModificationTime));
				processDirectory();
				changed = true;
				latestModificationTime = newLatestModificationTime;
				processedFiles = newProcessedFiles;
			} catch (final IOException | RuntimeException e) {
				logger.warn("Error in directory " + directory + ", no file processing...", e);
			}
		}

		logSummary(changed);
	}

	protected void logSummary(boolean changed) {
		if (changed) {
			String summary = processedFiles.stream().map(File::getName).collect(Collectors.joining(", "));
			logger.info("Processed files: {}", summary);
		}
	}
}
