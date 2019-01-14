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
package com.camline.projects.smardes.common.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class LoggerWriter extends PrintWriter {
	public LoggerWriter(final Logger logger, final Level level) {
		super(createWriter(logger, level), true);
	}

	private static final Writer createWriter(final Logger logger, final Level level) {
		return new InternalLoggerWriter(logger, level);
	}

	static class InternalLoggerWriter extends Writer {
		private final Logger logger;
		private final Level level;
		private boolean closed;

		public InternalLoggerWriter(final Logger logger, final Level level) {
			lock = logger;
			// synchronize on this logger
			this.logger = logger;
			this.level = level;
		}

		@Override
		public void write(final char[] cbuf, final int off, final int _len) throws IOException {
			if (closed) {
				throw new IOException("Called write on closed Writer");
			}

			final String msg = prepare(cbuf, off, _len);
			if (msg.isEmpty()) {
				return;
			}

			switch (level) {
			case ERROR:
				logger.error(msg);
				break;
			case WARN:
				logger.warn(msg);
				break;
			case INFO:
				logger.info(msg);
				break;
			case DEBUG:
				logger.debug(msg);
				break;
			case TRACE:
				logger.trace(msg);
				break;
			default:
				logger.warn("!!Unknown log level {}!! {}", level, msg);
				break;
			}
		}

		private static String prepare(final char[] cbuf, final int off, final int _len) {
			int len = _len;
			// Remove the end of line chars
			while (len > 0 && (cbuf[len - 1] == '\n' || cbuf[len - 1] == '\r')) {
				len--;
			}

			if (len <= 0) {
				return StringUtils.EMPTY;
			}

			return String.valueOf(cbuf, off, len);
		}

		@Override
		public void flush() throws IOException {
			if (closed) {
				throw new IOException("Called flush on closed Writer");
			}
		}

		@Override
		public void close() {
			closed = true;
		}
	}
}
