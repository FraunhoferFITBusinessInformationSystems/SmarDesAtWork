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
package com.camline.projects.smardes.common.br;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;

import com.camline.projects.smardes.jsonapi.SmarDesException;

public abstract class BaseBR<R> implements IBR {
	private final String brName;
	protected final Logger logger;
	private final Map<String, Object> context;
	private ZonedDateTime started;
	private boolean succeeded;

	protected BaseBR(final String brName, final Logger log) {
		this.brName = brName;
		this.logger = log;
		this.context = new LinkedHashMap<>();
		addContext("brName", brName);
	}

	/**
	 * Enters the business rule. Logs a line and remembers start time.
	 */
	private void begin() {
		logger.info("BEGIN {}.", brName);
		started = ZonedDateTime.now();
		addContext("started", started);
	}

	protected abstract void init();

	protected void markSucceeded() {
		succeeded = true;
	}


	protected boolean isSucceeded() {
		return succeeded;
	}

	protected abstract void cleanup();

	/**
	 * Leaves business rule. Logs a line with duration info.
	 */
	private void end() {
		final long duration = Duration.between(started.toInstant(), Instant.now()).toMillis();
		addContext("duration", duration);
		addContext("succeeded", String.valueOf(succeeded));
		logger.info(
				"END {} with {} in {} ms.\n\t{}",
				brName,
				succeeded ? "success" : "ERROR",
				Long.valueOf(duration),
				context);
	}

	@Override
	public final void addContext(final String name, final Object value) {
		context.put(name, value);
	}

	@Override
	public final void addContext(final String name, final boolean value) {
		context.put(name, Boolean.valueOf(value));
	}

	@Override
	public final void addContext(final String name, final long value) {
		context.put(name, new MutableLong(value));
	}

	@Override
	public final void incContext(final String name, final long additional) {
		final MutableLong value = (MutableLong) context.get(name);
		if (value != null) {
			value.add(additional);
			return;
		}

		addContext(name, additional);
	}

	public R execute(final BRCallable<R> callable) {
		begin();

		try {
			// Initialize sessions, transactions, ...
			init();

			// Call the real business logic
			final R result = callable.call(this);

			// Mark the business rule as successful, changes can be committed
			markSucceeded();

			return result;
		} catch (final RuntimeException e) {
			throw handleExceptions(e);
		} finally {
			// Cleanup transactions, sessions, etc.
			cleanup();
			end();
		}
	}

	public boolean execute(final BRBooleanCallable callable) {
		begin();

		try {
			// Initialize sessions, transactions, ...
			init();

			// Call the real business logic
			final boolean result = callable.call(this);

			// Mark the business rule as successful, changes can be committed
			markSucceeded();

			return result;
		} catch (final RuntimeException e) {
			throw handleExceptions(e);
		} finally {
			// Cleanup transactions, sessions, etc.
			cleanup();
			end();
		}
	}

	public int execute(final BRIntCallable callable) {
		begin();

		try {
			// Initialize sessions, transactions, ...
			init();

			// Call the real business logic
			final int result = callable.call(this);

			// Mark the business rule as successful, changes can be committed
			markSucceeded();

			return result;
		} catch (final RuntimeException e) {
			throw handleExceptions(e);
		} finally {
			// Cleanup transactions, sessions, etc.
			cleanup();
			end();
		}
	}

	public void execute(final BRVoidCallable callable) {
		begin();

		try {
			// Initialize sessions, transactions, ...
			init();

			// Call the real business logic
			callable.call(this);

			// Mark the business rule as successful, changes can be committed
			markSucceeded();
		} catch (final RuntimeException e) {
			throw handleExceptions(e);
		} finally {
			// Cleanup transactions, sessions, etc.
			cleanup();
			end();
		}
	}

	private RuntimeException handleExceptions(final Exception e) {
		logger.error("BR ended with exception", e);
		if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		}
		throw new SmarDesException("Encapsulated", e);
	}
}
