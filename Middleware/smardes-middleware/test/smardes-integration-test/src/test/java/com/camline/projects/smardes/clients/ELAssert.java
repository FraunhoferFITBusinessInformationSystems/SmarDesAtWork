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
package com.camline.projects.smardes.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.camline.projects.smardes.common.el.ExpressionEngine;

public class ELAssert {
	static void assertExprIsLocalISO8601(ExpressionEngine expressionEngine, String expression) {
		String iso8601Str = expressionEngine.evaluateExpression(expression);
		assertTrue(expression + " must be local ISO8601",
				iso8601Str.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?$"));
	}

	static void assertExprIsOffsetISO8601(ExpressionEngine expressionEngine, String expression) {
		String iso8601Str = expressionEngine.evaluateExpression(expression);
		assertTrue(expression + " must be ISO8601 with offset",
				iso8601Str.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[+-]\\d{2}:\\d{2}$"));
	}

    static void assertExprEquals(String message, Object expected, ExpressionEngine expressionEngine, String expression) {
    	assertEquals(message, expected, expressionEngine.evaluateExpression(expression));
    }

    static void assertExprEquals(Object expected, ExpressionEngine expressionEngine, String expression) {
    	assertEquals(expected, expressionEngine.evaluateExpression(expression));
    }

    static void assertExprNotNull(ExpressionEngine expressionEngine, String expression) {
    	assertNotNull(expressionEngine.evaluateExpression(expression));
    }

    static void assertExprNull(ExpressionEngine expressionEngine, String expression) {
    	assertNull(expressionEngine.evaluateExpression(expression));
    }
}
