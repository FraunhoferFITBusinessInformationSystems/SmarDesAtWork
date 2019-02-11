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
package com.camline.projects.smardes.rule;

import java.util.Collections;
import java.util.Set;

public class NamedConditions {
	static final NamedConditions EMPTY = new NamedConditions(Collections.emptySet(), Collections.emptySet());

	private final Set<String> fulfilled;
	private final Set<String> notFulFilled;

	NamedConditions(Set<String> fulfilled, Set<String> notFulFilled) {
		this.fulfilled = fulfilled;
		this.notFulFilled = notFulFilled;
	}

	public boolean conditionFails(final String condition) {
		if (condition == null) {
			/*
			 * Elements with no condition do not fail.
			 */
			return false;
		}

		if (condition.startsWith("!")) {
			/*
			 * Element with condition="!cond1". Therefore cond1 must be in the list if not fulfilled conditions.
			 */
			return !notFulFilled.contains(condition.substring(1));
		}

		/*
		 * Element with condition="!cond1". Therefore cond1 must be in the list if not fulfilled conditions.
		 */
		return !fulfilled.contains(condition);
	}
}
