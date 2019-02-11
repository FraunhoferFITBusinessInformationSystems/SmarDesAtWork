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
package com.camline.projects.smardes.common.el.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.camline.projects.smardes.common.el.ELFunction;
import com.camline.projects.smardes.common.el.ELFunctions;
import com.camline.projects.smardes.common.el.ExpressionEngine;

/**
 * EL String Functions
 *
 * Just tag each EL function with @ELFunction annotation.
 *
 * @author matze
 *
 */
@ELFunctions("el")
public final class ELSupportFunctions {

	private ELSupportFunctions() {
		// utility class
	}

	@ELFunction
	public static Map<String, Object> newStructure() {
		return new HashMap<>();
	}

	@ELFunction
	public static Set<? extends Object> mapKeys(Map<?,?> map) {
		return map.keySet();
	}

	@ELFunction
	public static Collection<? extends Object> mapValues(Map<?,?> map) {
		return map.values();
	}

	@ELFunction
	public static Collection<Object> add(final Collection<? super Object> collection, final Object... elements) {
		for (final Object element : elements) {
			collection.add(element);
		}
		return collection;
	}

	@ELFunction
	public static List<Object> list(final Object... elements) {
		final List<Object> list = new ArrayList<>();
		for (final Object element : elements) {
			list.add(element);
		}
		return list;
	}

	@ELFunction
	public static Set<Object> set(final Object... elements) {
		final Set<Object> set = new LinkedHashSet<>();
		for (final Object element : elements) {
			set.add(element);
		}
		return set;
	}

	@ELFunction
	public static List<Object> filter(final Iterable<?> collection, String iteratorVar, String filterExpression) {
		final List<Object> filtered = new ArrayList<>();

		ExpressionEngine expressionEngine = new ExpressionEngine();
		for (Object item : collection) {
			expressionEngine.setContextVariable(iteratorVar, item);
			boolean matches = expressionEngine.evaluateBooleanExpression(filterExpression);
			if (matches) {
				filtered.add(item);
			}
		}

		return filtered;
	}

	@ELFunction
	public static boolean contains(final Collection<Object> collection, final Object value) {
		return collection.contains(value);
	}

	@ELFunction
	public static boolean exists(Map<String, Object> baseObject, String property) {
		return baseObject.containsKey(property);
	}

	@ELFunction
	public static Object get(Map<String, Object> baseObject, String property, Object defaultValue) {
		return baseObject.containsKey(property) ? baseObject.get(property) : defaultValue;
	}

	@ELFunction
	public static List<Object> select(Iterable<?> list, String iteratorVar, String selectExpression) {
		ExpressionEngine expressionEngine = new ExpressionEngine();
		List<Object> result = new ArrayList<>();
		for (Object item : list) {
			expressionEngine.setContextVariable(iteratorVar, item);
			result.add(expressionEngine.evaluateExpression(selectExpression));
		}
		return result;
	}
}

