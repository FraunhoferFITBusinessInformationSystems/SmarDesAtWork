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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.common.el.ELFunction;
import com.camline.projects.smardes.common.el.ELFunctions;
import com.camline.projects.smardes.common.time.ISO8601Parser;

@ELFunctions("datetime")
public final class DateTimeFunctions {
	private static final Map<String, ChronoUnit> CHRONO_UNITS;
	static {
		CHRONO_UNITS = new HashMap<>();
		for (final ChronoUnit chronoUnit : ChronoUnit.values()) {
			CHRONO_UNITS.put(chronoUnit.name().toLowerCase(), chronoUnit);
		}
	}

	private DateTimeFunctions() {
		// utility class
	}

	@ELFunction
	public static ZonedDateTime now() {
		return ZonedDateTime.now();
	}

	@ELFunction
	public static Temporal nowPlus(final String chronoUnit, final int amount) {
		return plus(now(), chronoUnit, amount);
	}

	@ELFunction
	public static Temporal plus(final Temporal temporal, final String chronoUnit, final int amount) {
		return EnumUtils.getEnumIgnoreCase(ChronoUnit.class, chronoUnit).addTo(temporal, amount);
	}

	@ELFunction
	public static Temporal parseISO8601(final String dateTimeStr) {
		return ISO8601Parser.parse(dateTimeStr);
	}

	@ELFunction
	public static Temporal switchZone(final Object dateTimeObject, String zoneIdStr) {
		ZonedDateTime zdt;

		if (dateTimeObject instanceof Date) {
			Date date = (Date) dateTimeObject;
			zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		} else if (dateTimeObject instanceof Temporal) {
			Temporal temporal = (Temporal) dateTimeObject;
			try {
				zdt = ZonedDateTime.from(temporal);
			} catch (DateTimeException e) {
				if (temporal instanceof LocalDateTime) {
					zdt = ZonedDateTime.of((LocalDateTime) temporal, ZoneId.systemDefault());
				} else {
					throw e;
				}
			}
		} else {
			throw new DateTimeException("Invalid datetime object type " + dateTimeObject.getClass());
		}

		ZoneId zoneId = StringUtils.isEmpty(zoneIdStr) ? ZoneId.systemDefault() : ZoneId.of(zoneIdStr);
		return zdt.withZoneSameInstant(zoneId);
	}

	@ELFunction
	public static Temporal setZone(final Temporal temporal, String zoneIdStr) {
		ZoneId zoneId = StringUtils.isEmpty(zoneIdStr) ? ZoneId.systemDefault() : ZoneId.of(zoneIdStr);

		try {
			return ZonedDateTime.from(temporal).withZoneSameLocal(zoneId);
		} catch (DateTimeException e) {
			if (temporal instanceof LocalDateTime) {
				return ZonedDateTime.of((LocalDateTime) temporal, zoneId);
			}
			throw e;
		}
	}

	@ELFunction
	public static String format(final Temporal temporal, final String pattern) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return formatter.format(temporal);
	}

	@ELFunction
	public static boolean isFuture(final Temporal temporal1) {
		Instant instant1 = toInstant(temporal1);
		if (instant1 != null) {
			return compare(instant1, Instant.now()) > 0;
		}
		if (temporal1 instanceof LocalDateTime) {
			return compare((LocalDateTime) temporal1, LocalDateTime.now()) > 0;
		}
		throw new DateTimeException("Invalid datetime object " + temporal1);
	}

	@ELFunction
	public static boolean isPast(final Temporal temporal1) {
		Instant instant1 = toInstant(temporal1);
		if (instant1 != null) {
			return compare(instant1, Instant.now()) < 0;
		}
		if (temporal1 instanceof LocalDateTime) {
			return compare((LocalDateTime) temporal1, LocalDateTime.now()) < 0;
		}
		throw new DateTimeException("Invalid datetime object " + temporal1);
	}

	@ELFunction
	public static int compare(final Temporal temporal1, final Temporal temporal2) {
		Instant instant1 = toInstant(temporal1);
		Instant instant2 = toInstant(temporal2);

		if (instant1 != null && instant2 != null) {
			return compare(instant1, instant2);
		}

		if (temporal1 instanceof LocalDateTime && temporal2 instanceof LocalDateTime) {
			return compare((LocalDateTime) temporal1, (LocalDateTime) temporal2);
		}
		throw new DateTimeException("Invalid datetime objects " + temporal1 + " and " + temporal2);
	}

	private static int compare(Instant instant1, Instant instant2) {
		if (instant1.isBefore(instant2)) {
			return -1;
		}
		if (instant1.isAfter(instant2)) {
			return 1;
		}
		return 0;
	}

	private static int compare(LocalDateTime ldt1, LocalDateTime ldt2) {
		if (ldt1.isBefore(ldt2)) {
			return -1;
		}
		if (ldt1.isAfter(ldt2)) {
			return 1;
		}
		return 0;
	}

	private static Instant toInstant(final Temporal temporal) {
		if (temporal instanceof ZonedDateTime) {
			return ((ZonedDateTime)temporal).toInstant();
		}
		if (temporal instanceof Instant) {
			return (Instant) temporal;
		}
		if (temporal instanceof OffsetDateTime) {
			return ((OffsetDateTime)temporal).toInstant();
		}
		return null;
	}
}
