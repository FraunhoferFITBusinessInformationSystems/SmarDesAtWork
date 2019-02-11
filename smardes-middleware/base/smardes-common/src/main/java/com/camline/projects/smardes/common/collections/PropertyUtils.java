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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyUtils {
	private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

	public static final File CONFIG_DIR = new File("config");
	private static final File PROPERTIES_DIR = new File("properties");

	private PropertyUtils() {
		// utility class
	}

	/**
	 * Load a external component configuration from properties directory;
	 *
	 * @param fileName config file name without path
	 * @return Java properties file
	 * @throws IOException
	 */
	public static Properties loadSmardesConfiguration(final String fileName) throws IOException {
		return loadProperties(CONFIG_DIR, fileName);
	}

	/**
	 * Load a smardes modules configuration from config directory;
	 *
	 * @param fileName config file name without path
	 * @return Java properties file
	 * @throws IOException
	 */
	public static Properties loadExternalConfiguration(final String fileName) throws IOException {
		return loadProperties(PROPERTIES_DIR, fileName);
	}

	public static Properties loadProperties(final File dir, final String fileName) throws IOException {
		return loadProperties(new File(dir, fileName));
	}

	public static Properties loadProperties(final File propFile) throws IOException {
		final Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(propFile)) {
			properties.load(fis);
		}
		return properties;
	}

	/**
	 * Extract part of a properties file which starts with the same and turn all entries into Map
	 * Example:
	 * servers.server1.host=host1
	 * servers.server1.port=4711
	 * servers.server2.host=host2
	 * servers.server2.port=4712
	 *
	 * @return nested map with all properties with this prefix
	 */
	public static Map<String, Map<String, String>> groupProperties(final Map<?,?> properties, final String prefix) {
		final Map<String, Map<String, String>> groups = new HashMap<>();

		for (final Entry<?, ?> entry : properties.entrySet()) {
			String propertyName = entry.getKey().toString();
			if (!propertyName.startsWith(prefix + ".")) {
				continue;
			}

			String groupAndKey = propertyName.substring(prefix.length() + 1);

			/*
			 * Property name must follow convention prefix.groupName.keyName
			 */
			final String[] tokens = StringUtils.split(groupAndKey, '.');
			if (tokens.length != 2 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
				continue;
			}

			final String groupName = tokens[0];
			final String keyName = tokens[1];

			Map<String, String> group = groups.computeIfAbsent(groupName, key -> new HashMap<>());
			group.put(keyName, entry.getValue().toString());
		}

		return groups;
	}

	/**
	 * Simpler variant of groupProperties where only multiple values for one base property should
	 * be compiled together, e.g.
	 * pattern=a_first_pattern
	 * pattern.1=first_alternative_pattern
	 * pattern.2=second_alternative_pattern
	 *
	 * @param properties
	 * @param prefix
	 * @return
	 */
	public static Map<String, String> extractMultiProperties(final Map<?,?> properties, final String prefix) {
		final Map<String, String> multiProperties = new LinkedHashMap<>();

		for (final Entry<?, ?> entry : properties.entrySet()) {
			String propertyName = entry.getKey().toString();
			String key;
			if (propertyName.equals(prefix)) {
				key = "";
			} else if (propertyName.startsWith(prefix + ".")) {
				key = propertyName.substring(prefix.length() + 1);
			} else {
				continue;
			}

			multiProperties.put(key, entry.getValue().toString());
		}

		return multiProperties;
	}

	public static String getStringProperty(final Map<?,?> properties, final String name) {
		final String value = getOptionalStringProperty(properties, name);
		if (value != null) {
			return value;
		}
		throw new IllegalArgumentException("Undefined mandatory property " + name);
	}

	@SuppressWarnings("unlikely-arg-type")
	public static String getOptionalStringProperty(final Map<?,?> properties, final String name) {
		if (properties instanceof Properties) {
			return ((Properties)properties).getProperty(name);
		}
		return Objects.toString(properties.get(name), null);
	}

	public static String getStringProperty(final Map<?,?> properties, final String name, final String defaultValue) {
		return StringUtils.defaultString(getOptionalStringProperty(properties, name), defaultValue);
	}

	public static boolean getBooleanProperty(final Map<?,?> properties, final String name) {
		final String value = getStringProperty(properties, name);
		return Boolean.parseBoolean(value);
	}

	public static int getIntegerProperty(final Map<?,?> properties, final String name) {
		final String value = getStringProperty(properties, name);
		return Integer.parseInt(value);
	}

	public static double getDoubleProperty(final Map<?,?> properties, final String name) {
		final String value = getStringProperty(properties, name);
		return Double.parseDouble(value);
	}

	public static char getCharProperty(final Map<?,?> properties, final String name) {
		final String value = getStringProperty(properties, name);
		if (value.length() == 1) {
			return value.charAt(0);
		}
		throw new IllegalArgumentException(
				String.format("Mandatory property %s must be a single character, but is '%s'", name, value));
	}

	public static Character getOptionalCharProperty(final Map<?,?> properties, final String name) {
		final String value = getOptionalStringProperty(properties, name);
		if (value == null) {
			return null;
		}
		if (value.length() == 1) {
			return Character.valueOf(value.charAt(0));
		}
		throw new IllegalArgumentException(
				String.format("Mandatory property %s must be a single character, but is '%s'", name, value));
	}

	public static char getCharProperty(final Map<?,?> properties, final String name, char defaultValue) {
		final Character value = getOptionalCharProperty(properties, name);
		if (value == null) {
			return defaultValue;
		}
		return value.charValue();
	}

	public static <E extends Enum<E>> E getEnumIgnoreCaseProperty(final Class<E> enumClass, final Map<?, ?> properties,
			final String name) {
		final String value = getStringProperty(properties, name);
		E enumValue = EnumUtils.getEnumIgnoreCase(enumClass, value);
		if (enumValue != null) {
			return enumValue;
		}
		throw new IllegalArgumentException(String.format("Mandatory property %s must be a %s type, but is '%s'", name,
				enumClass.getSimpleName(), value));
	}

	public static Map<Object, Object> mergeProperties(File... propertyFiles) {
		Map<Object, Object> allProperties = new HashMap<>();

		for (File file : propertyFiles) {
			Properties properties;
			try {
				properties = PropertyUtils.loadProperties(file);
			} catch (final IOException e) {
				logger.error("Cannot read properties file " + file, e);
				properties = new Properties();
			}

			for (Entry<Object, Object> property : properties.entrySet()) {
				Object previousValue = allProperties.put(property.getKey(), property.getValue());
				if (previousValue != null) {
					logger.warn("Duplicate query entry with name {}:\n\tExisting value: {}\n\tNew value: {}",
							property.getKey(), previousValue, property.getValue());
				}
			}
		}

		return allProperties;
	}
}
