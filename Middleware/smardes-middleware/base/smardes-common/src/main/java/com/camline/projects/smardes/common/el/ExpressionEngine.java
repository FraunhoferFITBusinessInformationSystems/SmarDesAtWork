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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.el.functions.DateTimeFunctions;
import com.camline.projects.smardes.common.el.functions.ELSupportFunctions;
import com.camline.projects.smardes.common.el.functions.IntegerFunctions;
import com.camline.projects.smardes.common.el.functions.StringFunctions;
import com.camline.projects.smardes.common.el.functions.UUIDFunctions;

import de.odysseus.el.tree.TreeBuilderException;

public class ExpressionEngine {
	private static final Logger logger = LoggerFactory.getLogger(ExpressionEngine.class);

	private static final int ERROR_EXPR_CONTEXT_RADIUS = 10;

	private final ExpressionFactory factory;
	private final ExpressionContext elContext;

	public ExpressionEngine() {
		factory = new de.odysseus.el.ExpressionFactoryImpl();
		elContext = new ExpressionContext();
		registerFunctions(ELSupportFunctions.class, StringFunctions.class, IntegerFunctions.class, UUIDFunctions.class,
				DateTimeFunctions.class);
	}

	private void registerFunction(final String prefix, final Method method) {
		if (!method.isAnnotationPresent(ELFunction.class)) {
			return;
		}

		if (elContext.getFunctionMapper().resolveFunction(prefix, method.getName()) != null) {
			throw new IllegalArgumentException(
					String.format("EL function %s:%s already exists", prefix, method.getName()));
		}

		elContext.setFunction(prefix, method.getName(), method);
	}

	public final ExpressionEngine registerFunctions(final Class<?>... classes) {
		for (Class<?> klass : classes) {
			if (!klass.isAnnotationPresent(ELFunctions.class)) {
				logger.warn("Class {} is not an @ELFunctions class", klass.getSimpleName());
				continue;
			}
			final String prefix = klass.getAnnotation(ELFunctions.class).value();
			for (final Method method : klass.getMethods()) {
				registerFunction(prefix, method);
			}
		}
		return this;
	}

	public ExpressionEngine setContextVariable(final String name, final Object value) {
		logger.debug("Set top-level EL variable {}={}", name, value);
		elContext.setVariable(name,
				factory.createValueExpression(value, value != null ? value.getClass() : Object.class));
		return this;
	}

	public boolean existsContextVariable(final String name) {
		return elContext.existsVariable(name);
	}

	public ExpressionEngine removeContextVariable(final String name) {
		elContext.removeVariable(name);
		return this;
	}

	public ExpressionEngine setContextVariables(final Map<String, Object> values) {
		for (final Entry<String, Object> entry : values.entrySet()) {
			setContextVariable(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/**
	 * Evaulate expression and return the raw evaulation result.
	 *
	 * Use this only if you know for sure the expected type or you want to deal with Object anyway.
	 *
	 * @param expression EL expression
	 * @return raw evaluation result
	 */
	@SuppressWarnings("unchecked")
	public <T> T evaluateExpression(final String expression) {
		final ValueExpression expr = parseExpression(expression, Object.class);
		try {
			return (T) expr.getValue(elContext);
		} catch (final ELException e) {
			throw new ExpressionEvaluationException(expr.getExpressionString(), e);
		}
	}

	/**
	 * Evaluate expression and coerce it to the desired type (if possible).
	 * @param expression EL expression
	 * @param klass desired type to coerce to
	 * @return evaluation result coerced to the desired type
	 */
	@SuppressWarnings("unchecked")
	public <T> T evaluateExpression(final String expression, final Class<T> klass) {
		final ValueExpression expr = parseExpression(expression, klass);
		try {
			return (T) expr.getValue(elContext);
		} catch (final ELException e) {
			throw new ExpressionEvaluationException(expr.getExpressionString(), e);
		}
	}

	/**
	 * Evaluate expression and coerce it to a boolean if possible, e.g. string to boolean
	 * @param expression EL expression
	 * @return expression coerced to a boolean
	 */
	public boolean evaluateBooleanExpression(final String expression) {
		final Boolean result = evaluateExpression(expression, Boolean.class);
		if (result == null) {
			throw new ELException(
					String.format("Boolean expression ${%s} evaluates to null.", expression));
		}
		return result.booleanValue();
	}

	/**
	 * Evaluate expression and coerce it to a boolean if possible, e.g. string to boolean
	 * @param expression EL expression
	 * @return expression coerced to a boolean
	 */
	public long evaluateLongExpression(final String expression) {
		final Long result = evaluateExpression(expression, Long.class);
		if (result == null) {
			throw new ELException(
					String.format("Long expression ${%s} evaluates to null.", expression));
		}
		return result.longValue();
	}

	public void setValue(final String expression, final Object value) {
		final ValueExpression expr = parseExpression(expression, value != null ? value.getClass() : Object.class);
		expr.setValue(elContext, value);
	}

	private ValueExpression parseExpression(final String expression, final Class<?> klass) {
		final String elExpression = "${" + expression + "}";
		try {
			return factory.createValueExpression(elContext, elExpression, klass);
		} catch (final TreeBuilderException e) {
			int start = Math.max(e.getPosition() - ERROR_EXPR_CONTEXT_RADIUS, 0);
			int end = Math.min(e.getPosition() + ERROR_EXPR_CONTEXT_RADIUS, e.getExpression().length());
			String near = e.getExpression().substring(start, end);
			throw new ExpressionParseException(elExpression, near, e);
		} catch (final ELException e) {
			throw new ExpressionParseException(elExpression, e);
		}
	}

	public Map<String, Object> getContextVariables() {
		Map<String, Object> values = new HashMap<>();
		for (String name : elContext.getVariableNames()) {
			values.put(name, evaluateExpression(name));
		}
		return values;
	}
}
