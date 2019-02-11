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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.el.ValueExpression;
import javax.el.VariableMapper;

import de.odysseus.el.ObjectValueExpression;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.util.SimpleContext;

class ExpressionContext extends SimpleContext {
	private final Variables variables = new Variables();

	ExpressionContext() {
		super(new PedanticSimpleResolver());
	}

	/**
	 * Get our variable mapper.
	 */
	@Override
	public VariableMapper getVariableMapper() {
		return variables;
	}

	@Override
	public ValueExpression setVariable(String name, ValueExpression expression) {
		return variables.setVariable(name, expression);
	}

	ValueExpression removeVariable(String name) {
		return variables.removeVariable(name);
	}

	boolean existsVariable(final String name) {
		return variables.existsVariable(name);
	}

	Set<String> getVariableNames() {
		return variables.getVariableNames();
	}

	static class Variables extends VariableMapper {
		Map<String, ValueExpression> map = new HashMap<>();

		Variables() {
			map = new HashMap<>();
			map.put("root", new ObjectValueExpression(TypeConverter.DEFAULT, map, map.getClass()));
		}

		@Override
		public ValueExpression resolveVariable(String variable) {
			return map.get(variable);
		}

		@Override
		public ValueExpression setVariable(String variable, ValueExpression expression) {
			return map.put(variable, expression);
		}

		ValueExpression removeVariable(String variable) {
			return map.remove(variable);
		}

		boolean existsVariable(final String name) {
			return map.containsKey(name);
		}

		Set<String> getVariableNames() {
			return map.keySet();
		}
	}
}
