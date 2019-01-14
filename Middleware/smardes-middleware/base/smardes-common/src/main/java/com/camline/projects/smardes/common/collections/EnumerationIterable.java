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
package com.camline.projects.smardes.common.collections;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Convert an iterable from an enumeration.
 *
 * Implementation based on http://www.javaspecialists.eu/archive/Issue107.html.
 * Modified to deal with Enumeration<?> since enumerations are in almost all
 * cases untyped because of their legacy nature.
 *
 * @author matze
 *
 * @param <E>
 *            the type of the enumeration
 */
public class EnumerationIterable<E> implements Iterable<E> {
	private final Enumeration<?> en;

	public EnumerationIterable(final Enumeration<?> en) {
		this.en = en;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			@Override
			public boolean hasNext() {
				return en.hasMoreElements();
			}

			@Override
			@SuppressWarnings("unchecked")
			public E next() {
				return (E) en.nextElement();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static <E> Iterable<E> create(final Enumeration<?> en) {
		return new EnumerationIterable<>(en);
	}
}
