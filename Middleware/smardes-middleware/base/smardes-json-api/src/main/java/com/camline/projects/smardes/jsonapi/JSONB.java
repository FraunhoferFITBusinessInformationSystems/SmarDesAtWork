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
package com.camline.projects.smardes.jsonapi;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.json.JsonException;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.config.BinaryDataStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSONB {
	private static final Logger logger = LoggerFactory.getLogger(JSONB.class);
	private static final JSONB instance = new JSONB();
	private final Jsonb jsonb;

	private JSONB() {
		final JsonbConfig config = new JsonbConfig()
				.withFormatting(Boolean.TRUE)
				.withBinaryDataStrategy(BinaryDataStrategy.BASE_64)
				.withAdapters(new TimestampAdapter(), new ZonedDateTimeAdapter(), new UUIDAdapter());
		jsonb = JsonbBuilder.create(config);
	}

	public static JSONB instance() {
		return instance;
	}

	public <T> T unmarshalBody(final byte[] raw, final Class<T> klass) {
		if (raw == null) {
			return null;
		}
		return jsonb.fromJson(new String(raw, StandardCharsets.UTF_8), klass);
	}

	public <T> T unmarshalBody(final String jsonBody, final Class<T> klass) {
		if (jsonBody == null) {
			return null;
		}

		return jsonb.fromJson(jsonBody, klass);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> unmarshalBodyGeneric(final byte[] raw) {
		try {
			return jsonb.fromJson(new String(raw, StandardCharsets.UTF_8), Map.class);
		} catch (JsonException | JsonbException e) {
			logger.warn("Could not parse the body as JSON", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> unmarshalBodyGeneric(final String jsonBody) {
		try {
			return jsonb.fromJson(jsonBody, Map.class);
		} catch (JsonException | JsonbException e) {
			logger.warn("Could not parse the body as JSON", e);
			return null;
		}
	}

	public void dumpObject(final String name, final Object jsonbObj) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Dump object {}: {}", name, jsonb.toJson(jsonbObj));
			}
		} catch (final RuntimeException e) {
			logger.debug("Could not dump object " + name + ": ", e);
		}
	}

	public String marshalToString(final Object jsonbObj) {
		return jsonb.toJson(jsonbObj);
	}

	public byte[] marshal(final Object jsonbObj) {
		final String json = marshalToString(jsonbObj);
		return json.getBytes(StandardCharsets.UTF_8);
	}

	public void close() {
		try {
			jsonb.close();
		} catch (final Exception e) {
			logger.warn("Error when closing JSON-B builder. Ignoring...", e);
		}
	}
}
