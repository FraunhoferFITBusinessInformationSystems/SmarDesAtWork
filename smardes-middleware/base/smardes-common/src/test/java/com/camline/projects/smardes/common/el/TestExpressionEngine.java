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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;

public class TestExpressionEngine {
	private static final ZoneId MY_ZONE = ZoneId.of("Europe/Berlin");

	private ExpressionEngine expressionEngine;

	@Before
	public void initExpressionEngine() {
		expressionEngine = new ExpressionEngine();
	}

	@Test
	public void testTypeCoercion() {
		expressionEngine.setContextVariable("string1", "string1");
		expressionEngine.setContextVariable("int1", Integer.valueOf(123));
		expressionEngine.setContextVariable("boolean1", Boolean.TRUE);

		assertEquals("string1", expressionEngine.evaluateExpression("string1", String.class));
		assertEquals("string1", expressionEngine.evaluateExpression("string1"));

		assertEquals("123", expressionEngine.evaluateExpression("int1", String.class));
		assertEquals(Integer.valueOf(123), expressionEngine.evaluateExpression("int1"));

		assertEquals("true", expressionEngine.evaluateExpression("boolean1", String.class));
		assertEquals(Boolean.TRUE, expressionEngine.evaluateExpression("boolean1"));

		try {
			expressionEngine.evaluateExpression("string1", Integer.class);
			fail("This should raise an exception");
		} catch (ExpressionEvaluationException e) {
			assertEquals("Error in evaluating expression ${string1}", e.getMessage());
		}

		assertEquals(Boolean.FALSE, expressionEngine.evaluateExpression("string1", Boolean.class));
		assertEquals(Boolean.TRUE, expressionEngine.evaluateExpression("'true'", Boolean.class));

		try {
			expressionEngine.evaluateBooleanExpression("0");
			fail("This should raise an exception");
		} catch (ExpressionEvaluationException e) {
			assertEquals("Error in evaluating expression ${0}", e.getMessage());
		}
	}

	private Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime> parseZonedDateTime(String iso8601) {
		String expression0 = "datetime:parseISO8601('" + iso8601 + "')";
		ZonedDateTime zdt1 = expressionEngine.evaluateExpression(expression0);
		System.out.println("'" + expression0 + "' evaluates to " + zdt1);

		String expression = "datetime:switchZone(" + expression0 + ", 'Europe/Berlin')";
		ZonedDateTime zdt2 = expressionEngine.evaluateExpression(expression);
		System.out.println("Switch zone to Europe/Berlin gives " + zdt2);

		expression = "datetime:setZone(" + expression0 + ", 'Europe/Berlin')";
		ZonedDateTime zdt3 = expressionEngine.evaluateExpression(expression);
		System.out.println("Overwrite zone to Europe/Berlin gives " + zdt3);

		return Triple.of(zdt1, zdt2, zdt3);
	}

	private Triple<LocalDateTime, ZonedDateTime, ZonedDateTime> parseLocalDateTime(String iso8601) {
		String expression0 = "datetime:parseISO8601('" + iso8601 + "')";
		LocalDateTime ldt = expressionEngine.evaluateExpression(expression0);
		System.out.println("'" + expression0 + "' evaluates to " + ldt);

		String expression = "datetime:switchZone(" + expression0 + ", 'Europe/Berlin')";
		ZonedDateTime zdt2 = expressionEngine.evaluateExpression(expression);
		System.out.println("Switch zone to Europe/Berlin gives " + zdt2);

		expression = "datetime:setZone(" + expression + ", 'Europe/Berlin')";
		ZonedDateTime zdt3 = expressionEngine.evaluateExpression(expression);
		System.out.println("Overwrite zone to Europe/Berlin gives " + zdt3);

		return Triple.of(ldt, zdt2, zdt3);
	}

	@Test
	public void testNow() {
		ZonedDateTime now = expressionEngine.evaluateExpression("datetime:now()");
		assertEquals(MY_ZONE, now.getZone());
	}

	@Test
	public void testParseISO8601() {
		Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime> zdtzdtzdt;
		Triple<LocalDateTime, ZonedDateTime, ZonedDateTime> ldtzdtzdt;

		/*
		 * UTC time stamp and switch to Berlin
		 */
		zdtzdtzdt = parseZonedDateTime("2018-03-26T07:38:06Z");
		assertEquals(7, zdtzdtzdt.getLeft().getHour());
		assertEquals("Z", zdtzdtzdt.getLeft().getOffset().getId());
		assertEquals(0, zdtzdtzdt.getLeft().getOffset().getTotalSeconds());
		assertEquals("Z", zdtzdtzdt.getLeft().getZone().getId());
		assertEquals(9, zdtzdtzdt.getMiddle().getHour());
		assertEquals("+02:00", zdtzdtzdt.getMiddle().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getMiddle().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getMiddle().getZone().getId());
		assertEquals(7, zdtzdtzdt.getRight().getHour());
		assertEquals("+02:00", zdtzdtzdt.getRight().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getRight().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getRight().getZone().getId());

		/*
		 * Fixed +02:00 time stamp and switch to Berlin which does not change the time (since 2018-03-26 is in DST)
		 */
		zdtzdtzdt = parseZonedDateTime("2018-03-26T17:38:06+02:00");
		assertEquals(17, zdtzdtzdt.getLeft().getHour());
		assertEquals("+02:00", zdtzdtzdt.getLeft().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getLeft().getOffset().getTotalSeconds());
		assertEquals("+02:00", zdtzdtzdt.getLeft().getZone().getId());
		assertEquals(17, zdtzdtzdt.getMiddle().getHour());
		assertEquals("+02:00", zdtzdtzdt.getMiddle().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getMiddle().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getMiddle().getZone().getId());
		assertEquals(17, zdtzdtzdt.getRight().getHour());
		assertEquals("+02:00", zdtzdtzdt.getRight().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getRight().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getRight().getZone().getId());

		/*
		 * Fixed +02:00 time stamp and switch to Berlin which does not change the time (since 2018-02-26 not in DST)
		 */
		zdtzdtzdt = parseZonedDateTime("2018-02-26T17:38:06+02:00");
		assertEquals(17, zdtzdtzdt.getLeft().getHour());
		assertEquals("+02:00", zdtzdtzdt.getLeft().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getLeft().getOffset().getTotalSeconds());
		assertEquals("+02:00", zdtzdtzdt.getLeft().getZone().getId());
		assertEquals(16, zdtzdtzdt.getMiddle().getHour());
		assertEquals("+01:00", zdtzdtzdt.getMiddle().getOffset().getId());
		assertEquals(3600, zdtzdtzdt.getMiddle().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getMiddle().getZone().getId());
		assertEquals(17, zdtzdtzdt.getRight().getHour());
		assertEquals("+01:00", zdtzdtzdt.getRight().getOffset().getId());
		assertEquals(3600, zdtzdtzdt.getRight().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getRight().getZone().getId());

		/*
		 * Local Berlin time stamp and switch to Berlin which does not change the time
		 */
		zdtzdtzdt = parseZonedDateTime("2018-03-26T15:38:06+02:00[Europe/Berlin]");
		assertEquals(15, zdtzdtzdt.getLeft().getHour());
		assertEquals("+02:00", zdtzdtzdt.getLeft().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getLeft().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getLeft().getZone().getId());
		assertEquals(15, zdtzdtzdt.getMiddle().getHour());
		assertEquals("+02:00", zdtzdtzdt.getMiddle().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getMiddle().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getMiddle().getZone().getId());
		assertEquals(15, zdtzdtzdt.getRight().getHour());
		assertEquals("+02:00", zdtzdtzdt.getRight().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getRight().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getRight().getZone().getId());

		/*
		 * Local Berlin time stamp but in winter time which should be fixed on switch
		 */
		zdtzdtzdt = parseZonedDateTime("2018-03-26T15:38:06Z[Europe/Berlin]");
		assertEquals(15, zdtzdtzdt.getLeft().getHour());
		assertEquals("+02:00", zdtzdtzdt.getLeft().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getLeft().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getLeft().getZone().getId());
		assertEquals(15, zdtzdtzdt.getMiddle().getHour());
		assertEquals("+02:00", zdtzdtzdt.getMiddle().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getMiddle().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getMiddle().getZone().getId());
		assertEquals(15, zdtzdtzdt.getRight().getHour());
		assertEquals("+02:00", zdtzdtzdt.getRight().getOffset().getId());
		assertEquals(7200, zdtzdtzdt.getRight().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", zdtzdtzdt.getRight().getZone().getId());

		ldtzdtzdt = parseLocalDateTime("2018-03-26T15:38:06");
		assertEquals(15, ldtzdtzdt.getLeft().getHour());
		assertEquals(Month.MARCH, ldtzdtzdt.getLeft().getMonth());
		assertEquals(15, ldtzdtzdt.getMiddle().getHour());
		assertEquals("+02:00", ldtzdtzdt.getMiddle().getOffset().getId());
		assertEquals(7200, ldtzdtzdt.getMiddle().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", ldtzdtzdt.getMiddle().getZone().getId());
		assertEquals(15, ldtzdtzdt.getRight().getHour());
		assertEquals("+02:00", ldtzdtzdt.getRight().getOffset().getId());
		assertEquals(7200, ldtzdtzdt.getRight().getOffset().getTotalSeconds());
		assertEquals("Europe/Berlin", ldtzdtzdt.getRight().getZone().getId());
	}

	@Test
	public void testStringFunctions() {
		List<String> abc = Arrays.asList("A", "B", "C");
		assertEquals(abc, expressionEngine.evaluateExpression("str:split('A:B:C', ':')"));
		assertEquals(abc, expressionEngine.evaluateExpression("str:split('A:B;C', ':;')"));
		assertEquals(abc, expressionEngine.evaluateExpression("str:split('A::B;:C', ':;')"));
		assertEquals("begin[A;B;C]end", expressionEngine.evaluateExpression("str:aggregate(str:split('A:B:C', ':'), ';', 'begin[', ']end')"));
	}

	@Test
	public void testSetValueFunctions() {
		Integer one = Integer.valueOf(1);
		expressionEngine.setContextVariable("map", new HashMap<>());
		expressionEngine.setValue("map.a", one);

		Map<String, Object> map = new HashMap<>();
		map.put("a", one);

		assertEquals(one, expressionEngine.evaluateExpression("map.a"));
		assertEquals(map, expressionEngine.evaluateExpression("map"));
	}

	@Test
	public void testELSupportFunctions() {
		expressionEngine.setContextVariable("elsupport", new HashMap<>());

		expressionEngine.setValue("elsupport.structure", expressionEngine.evaluateExpression("el:newStructure()"));
		Map<String, Object> reference = new HashMap<>();
		assertEquals(reference, expressionEngine.evaluateExpression("elsupport.structure"));

		Number onetwothree = Long.valueOf(123);
		Number twothreefour = Long.valueOf(234);

		expressionEngine.setValue("elsupport.structure.a", expressionEngine.evaluateExpression("123"));
		reference.put("a", onetwothree);
		assertEquals(reference, expressionEngine.evaluateExpression("elsupport.structure"));

		assertEquals(onetwothree, expressionEngine.evaluateExpression("elsupport.structure.a"));
		assertEquals(onetwothree, expressionEngine.evaluateExpression("el:get(elsupport.structure, 'a', 234)"));
		assertTrue(expressionEngine.evaluateBooleanExpression("el:get(elsupport.structure, 'a', 234) eq 123"));
		assertEquals(twothreefour, expressionEngine.evaluateExpression("el:get(elsupport.structure, 'b', 234)"));
		assertTrue(expressionEngine.evaluateBooleanExpression("el:get(elsupport.structure, 'b', 234) eq 234"));

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		map.put("key4", "value4");
		map.put("key5", "value5");
		map.put("key6", "value6");

		expressionEngine.setValue("elsupport.map", map);
		Collection<?> mapValues = expressionEngine.evaluateExpression("el:mapValues(elsupport.map)");
		assertEquals(map.values(), mapValues);

		List<?> filter = expressionEngine.evaluateExpression(
				"el:filter(el:mapValues(elsupport.map), 'each', 'str:indexOf(each, \\'3\\') > 0')");
		assertEquals(Arrays.asList("value3"), filter);

		Set<String> set = expressionEngine.evaluateExpression("el:add(el:set('A', 'B', 'C'), 'D','E')");
		Set<String> set2 = Stream.of("E", "D", "C", "B", "A").collect(Collectors.toSet());
		assertEquals(set2, set);

		expressionEngine.setValue("elsupport.maps", Arrays.asList(map, map, map));
		List<String> result = expressionEngine.evaluateExpression("el:select(elsupport.maps, 'each', 'each.key4')");
		assertEquals(Arrays.asList("value4", "value4", "value4"), result);
	}

}
