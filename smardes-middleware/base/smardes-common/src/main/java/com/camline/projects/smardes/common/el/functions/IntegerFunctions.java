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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.camline.projects.smardes.common.el.ELFunction;
import com.camline.projects.smardes.common.el.ELFunctions;

/**
 * EL Integer Functions
 *
 * Just tag each EL function with @ELFunction annotation.
 *
 * @author matze
 *
 */
@ELFunctions("int")
public final class IntegerFunctions {
	private static final int SEQUENCE_START = 100_000;
	private static AtomicInteger seq = new AtomicInteger(SEQUENCE_START);
	private static Random random = new Random();

	private IntegerFunctions() {
		// utility class
	}

	@ELFunction
	public static int randomRange(final int start, final int end) {
		return start + random.nextInt(end - start);
	}

	@ELFunction
	public static int nextSequence() {
		return seq.incrementAndGet();
	}
}
