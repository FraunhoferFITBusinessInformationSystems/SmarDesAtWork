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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileMonitor<T> implements Runnable {
	private static final List<String> CONTENT_TYPE_BLACKLIST = Arrays.asList("text/html");

	private static final int HTTP_STATUS_OK = 200;

	private static final int DEFAULT_READ_TIMEOUT = 5000;

	private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

	private static final Logger logger = LoggerFactory.getLogger(FileMonitor.class);

	private final URL url;
	private T lastResult;
	private byte[] oldContent;

	public FileMonitor(final URL url) {
		this.url = url;
	}

	protected abstract T processContent(byte[] content, T oldResult)
			throws IOException;

	/**
	 * React on file deletion.
	 *
	 * @param file file that was deleted
	 * @param oldResult last processFile result with this file
	 * @throws IOException on any I/O error
	 */
	protected void processDeletedFile(final File file, final T oldResult) throws IOException {
		throw new UnsupportedOperationException("This monitor is not prepared for file deletion.");
	}

	@Override
	public void run() {
		HttpURLConnection httpURLConnection = null;
		try {
			URLConnection urlConnection = url.openConnection();
			if (urlConnection instanceof HttpURLConnection) {
				httpURLConnection = (HttpURLConnection) urlConnection;
				boolean ok = prepare(httpURLConnection);
				if (!ok) {
					return;
				}
			}

			try (InputStream is = urlConnection.getInputStream()) {
				byte[] content = IOUtils.toByteArray(is);

				if (lastResult == null) {
					logger.info("Processing new file from {}...", url);
				} else if (!Arrays.equals(oldContent, content)) {
					logger.info("File {} has been changed. Processing...", url);
				} else {
					logger.info("File {} unchanged. Nothing to do...", url);
					return;
				}

				lastResult = processContent(content, lastResult);
				oldContent = content;
			}
		} catch (final IOException | RuntimeException e) {
			logger.warn("Error in file " + url + ", no file processing...", e);
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}
	}

	private static boolean prepare(HttpURLConnection urlConnection) throws IOException {
		urlConnection.setRequestMethod("GET");
		urlConnection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
		urlConnection.setReadTimeout(DEFAULT_READ_TIMEOUT);

		int status = urlConnection.getResponseCode();
		if (status != HTTP_STATUS_OK) {
			logger.warn("HTTP request return with code {}", Integer.valueOf(status));
			return false;
		}

		String contentType = urlConnection.getContentType();
		if (CONTENT_TYPE_BLACKLIST.stream().anyMatch(contentType::startsWith)) {
			logger.warn("Response is of blacklisted contentType {}. Ignoring....", contentType);
			return false;
		}

		return true;
	}
}
