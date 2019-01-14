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
package com.camline.projects.smardes.common.el;

import java.util.stream.Stream;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.ResourceBundleELResolver;

import de.odysseus.el.util.SimpleResolver;

/**
 * A variant of {@link SimpleResolver} but with a pedantic MapELResolver.
 *
 * @author matze
 *
 */
public class PedanticSimpleResolver extends SimpleResolver {
	private static final ELResolver DEFAULT_RESOLVER_READ_ONLY;
	private static final ELResolver DEFAULT_RESOLVER_READ_WRITE;
	static {
		CompositeELResolver resolver;

		resolver = new CompositeELResolver();
		Stream.of(new ArrayELResolver(true), new ListELResolver(true), new PedanticMapELResolver(true),
				new ResourceBundleELResolver(), new BeanELResolver(true))
				.forEach(resolver::add);
		DEFAULT_RESOLVER_READ_ONLY = resolver;

		resolver = new CompositeELResolver();
		Stream.of(new ArrayELResolver(false), new ListELResolver(false), new PedanticMapELResolver(false),
				new ResourceBundleELResolver(), new BeanELResolver(false))
				.forEach(resolver::add);
		DEFAULT_RESOLVER_READ_WRITE = resolver;
	}

	public PedanticSimpleResolver() {
		this(DEFAULT_RESOLVER_READ_WRITE, false);
	}

	public PedanticSimpleResolver(boolean readOnly) {
		this(readOnly ? DEFAULT_RESOLVER_READ_ONLY : DEFAULT_RESOLVER_READ_WRITE, readOnly);
	}

	public PedanticSimpleResolver(ELResolver resolver, boolean readOnly) {
		super(resolver, readOnly);
	}

	public PedanticSimpleResolver(ELResolver resolver) {
		super(resolver);
	}
}
