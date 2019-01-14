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
package com.camline.projects.smardes.dq;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BR;
import com.camline.projects.smardes.common.el.ELFunction;
import com.camline.projects.smardes.common.el.ELFunctions;

/**
 * MainData EL Functions
 *
 * Just tag each EL function with @ELFunction annotation.
 *
 * @author matze
 *
 */
@ELFunctions("dq")
public final class DataQueryFunctions {
	private static final Logger logger = LoggerFactory.getLogger(DataQueryFunctions.class);

	private DataQueryFunctions() {
		// utility class
	}

	@ELFunction
	public static List<Map<String, Object>> mapEntries(final String queryName, final Object... bindParameters) {
		return BR.execute(logger, new DataQueryBR(queryName, -1, Arrays.asList(bindParameters)));
	}

	@ELFunction
	public static Map<String, Object> mapEntry(final String queryName, final Object... bindParameters) {
		List<Map<String, Object>> entries = BR.execute(logger, new DataQueryBR(queryName, 2, Arrays.asList(bindParameters)));
		return entries != null && entries.size() == 1 ? entries.get(0) : null;
	}

	@ELFunction
	public static List<Object> entries(final String queryName, final Object... bindParameters) {
		return getEntries(queryName, -1, bindParameters);
	}

	@ELFunction
	public static Object entry(final String queryName, final Object... bindParameters) {
		final List<Object> entries = getEntries(queryName, 2, bindParameters);
		return entries != null && entries.size() == 1 ? entries.get(0) : null;
	}

	private static List<Object> getEntries(final String queryName, final int maxRecords, final Object... bindParameters) {
		final List<Map<String, Object>> result = BR.execute(logger,
				new DataQueryBR(queryName, maxRecords, Arrays.asList(bindParameters)));
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		final int mapSize = result.get(0).size();
		if (mapSize != 1) {
			throw new UnsupportedOperationException("Query must have only one field, but has " + mapSize);
		}
		return result.stream().map(map -> map.values().iterator().next()).collect(Collectors.toList());
	}
}
