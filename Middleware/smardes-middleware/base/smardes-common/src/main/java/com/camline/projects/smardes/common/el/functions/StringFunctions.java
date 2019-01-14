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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.common.el.ELFunction;
import com.camline.projects.smardes.common.el.ELFunctions;

/**
 * EL String Functions
 *
 * Just tag each EL function with @ELFunction annotation.
 *
 * @author matze
 *
 */
@ELFunctions("str")
public final class StringFunctions {
	private StringFunctions() {
		// utility class
	}

	@ELFunction
	public static String concat(final String... strings) {
		final StringBuilder sb = new StringBuilder();
		for (final String string : strings) {
			sb.append(string);
		}
		return sb.toString();
	}

	@ELFunction
	public static int indexOf(String str1, String sep) {
		return str1.indexOf(sep);
	}

	@ELFunction
	public static int lastIndexOf(String str1, String sep) {
		return str1.lastIndexOf(sep);
	}

	@ELFunction
	public static String replaceFirst(String str1, String regex, String replacement) {
		return str1.replaceFirst(regex, replacement);
	}

	@ELFunction
	public static String substr(final String str1, final int beginIndex, final int endIndex) {
		return str1.substring(beginIndex, endIndex);
	}

	@ELFunction
	public static String rsubstr(final String str1, final int beginIndex, final int endIndex) {
		final String tmp = new StringBuilder(str1).reverse().substring(beginIndex, endIndex);
		return new StringBuilder(tmp).reverse().toString();
	}

	@ELFunction
	public static String trim(final String str) {
		return str.trim();
	}

	@ELFunction
	public static List<String> split(final String str, final String separatorChars) {
		return Arrays.asList(StringUtils.split(str, separatorChars));
	}

	@ELFunction
	public static String aggregate(final Collection<String> strings, String delimiter, String prefix, String suffix) {
		return strings.stream().collect(Collectors.joining(delimiter, prefix, suffix));
	}
}
