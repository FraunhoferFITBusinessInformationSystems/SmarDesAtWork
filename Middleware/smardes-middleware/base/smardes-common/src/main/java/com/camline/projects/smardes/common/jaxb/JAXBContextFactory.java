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
/*
 * This class is based on a class implemented by Netflix and released under Apache License.
 * It was modified to integrate in the software environment - mainly javadoc and slight
 * modification for use with Java 7. Below is the original copyright notice.
 *
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camline.projects.smardes.common.jaxb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

/**
 * Creates and caches JAXB contexts as well as creates Marshallers and
 * Unmarshallers for each context. Slightly modified for Java 7 and Javadoc
 * added by Matze.
 */
public final class JAXBContextFactory {

	private final ConcurrentHashMap<Set<Class<?>>, JAXBContext> jaxbContexts =
			new ConcurrentHashMap<>(500);
	private final Map<String, Object> properties;

	private JAXBContextFactory(final Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * Creates a new {@link javax.xml.bind.Unmarshaller} that handles the
	 * supplied class.
	 *
	 * @param clazz
	 *            target class where the XML is unmarshalled into
	 * @param additionalClasses
	 *            optional additional classes from foreign schemas
	 * @return unmarshaller unmarshaller for this class
	 * @throws JAXBException
	 *             exceptions propagated by JAXB API calls
	 */
	public Unmarshaller createUnmarshaller(final Class<?> clazz,
			final Class<?>... additionalClasses) throws JAXBException {
		final JAXBContext ctx = getContext(clazz, additionalClasses);
		return ctx.createUnmarshaller();
	}

	/**
	 * Creates a new {@link javax.xml.bind.Marshaller} that handles the supplied
	 * class.
	 *
	 * @param clazz
	 *            source class to be marshalled
	 * @param additionalClasses
	 *            optional additional classes from foreign schemas
	 * @return marshaller for this class
	 * @throws JAXBException
	 *             exceptions propagated by JAXB API calls
	 */
	public Marshaller createMarshaller(final Class<?> clazz,
			final Class<?>... additionalClasses) throws JAXBException {
		final JAXBContext ctx = getContext(clazz, additionalClasses);
		final Marshaller marshaller = ctx.createMarshaller();
		setMarshallerProperties(marshaller);
		return marshaller;
	}

	private void setMarshallerProperties(final Marshaller marshaller)
			throws PropertyException {
		final Iterator<String> keys = properties.keySet().iterator();

		while (keys.hasNext()) {
			final String key = keys.next();
			marshaller.setProperty(key, properties.get(key));
		}
	}

	private JAXBContext getContext(final Class<?> baseClass,
			final Class<?>... additionalClasses) throws JAXBException {
		final Set<Class<?>> classes = new HashSet<>();
		classes.add(baseClass);
		for (final Class<?> clazz : additionalClasses) {
			classes.add(clazz);
		}

		JAXBContext jaxbContext = this.jaxbContexts.get(classes);
		if (jaxbContext == null) {
			final Class<?>[] allClasses = new Class<?>[additionalClasses.length+1];
			allClasses[0] = baseClass;
			System.arraycopy(
					additionalClasses,
					0,
					allClasses,
					1,
					additionalClasses.length);
			jaxbContext = JAXBContext.newInstance(allClasses);
			this.jaxbContexts.putIfAbsent(classes, jaxbContext);
		}
		return jaxbContext;
	}

	/**
	 * Creates instances of {@link JAXBContextFactory}
	 */
	public static class Builder {

		private final Map<String, Object> properties = new HashMap<>(5);

		/**
		 * Sets the jaxb.encoding property of any Marshaller created by this
		 * factory.
		 *
		 * @param value
		 *            property value
		 * @return builder object again for chaining calls
		 */
		public Builder withMarshallerJAXBEncoding(final String value) {
			properties.put(Marshaller.JAXB_ENCODING, value);
			return this;
		}

		/**
		 * Sets the jaxb.schemaLocation property of any Marshaller created by
		 * this factory.
		 *
		 * @param value
		 *            property value
		 * @return builder object again for chaining calls
		 */
		public Builder withMarshallerSchemaLocation(final String value) {
			properties.put(Marshaller.JAXB_SCHEMA_LOCATION, value);
			return this;
		}

		/**
		 * Sets the jaxb.noNamespaceSchemaLocation property of any Marshaller
		 * created by this factory.
		 *
		 * @param value
		 *            property value
		 * @return builder object again for chaining calls
		 */
		public Builder withMarshallerNoNamespaceSchemaLocation(final String value) {
			properties.put(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, value);
			return this;
		}

		/**
		 * Sets the jaxb.formatted.output property of any Marshaller created by
		 * this factory.
		 *
		 * @param value
		 *            property value
		 * @return builder object again for chaining calls
		 */
		public Builder withMarshallerFormattedOutput(final boolean value) {
			properties.put(
					Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.valueOf(value));
			return this;
		}

		/**
		 * Sets the jaxb.fragment property of any Marshaller created by this
		 * factory.
		 *
		 * @param value
		 *            property value
		 * @return builder object again for chaining calls
		 */
		public Builder withMarshallerFragment(final boolean value) {
			properties.put(Marshaller.JAXB_FRAGMENT, Boolean.valueOf(value));
			return this;
		}

		/**
		 * Creates a new {@link JAXBContextFactory} instance.
		 *
		 * @return JAXBContextFactory
		 */
		public JAXBContextFactory build() {
			return new JAXBContextFactory(properties);
		}
	}
}
