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
package com.camline.projects.smardes.common.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

/**
 * An utility class for JAXB marshalling/unmarshalling.
 *
 * It has the following advantages over existing variants in various camLine
 * software projects:
 * <ul>
 * <li>it uses a JAXContextCache correctly</li>
 * <li>the cache supports multi-threading and is lock-free</li>
 * <li>unmarshal convenience method using generics so a cast in the caller is
 * not required</li>
 * </ul>
 *
 * @author matze
 */
public final class JAXBUtils {
	private static final JAXBContextFactory JAXB_CONTEXT_FACTORY;
	static {
		final JAXBContextFactory.Builder builder = new JAXBContextFactory.Builder();
		builder.withMarshallerFormattedOutput(true);
		JAXB_CONTEXT_FACTORY = builder.build();
	}

	private JAXBUtils() {
		// utility class
	}

	/**
	 * This method marshals a JAXB object into an OutputStream.
	 *
	 * @param obj
	 *            JAXB object
	 * @param outputstream
	 *            target output stream
	 * @param additionalClasses
	 *            optional additional classes from foreign schemas
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static void marshal(final Object obj, final OutputStream outputstream,
			final Class<?>... additionalClasses) throws JAXBException {
		final Marshaller marshaller = JAXB_CONTEXT_FACTORY
				.createMarshaller(obj.getClass(), additionalClasses);
		marshaller.marshal(obj, outputstream);
	}

	/**
	 * Marshal a JAXB object into a byte array.
	 *
	 * @param obj
	 *            JAXB object
	 * @param additionalClasses
	 *            optional additional classes from foreign schemas
	 * @return marshalled object as byte array
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static byte[] marshalToBytes(final Object obj,
			final Class<?>... additionalClasses) throws JAXBException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshal(obj, baos, additionalClasses);
		return baos.toByteArray();
	}

	/**
	 * Marshal a JAXB object into a string
	 *
	 * @param obj
	 *            JAXB object
	 * @param additionalClasses
	 *            optional additional classes from foreign schemas
	 * @return marshalled object a string
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static String marshalToString(final Object obj,
			final Class<?>... additionalClasses) throws JAXBException {
		final StringWriter sw = new StringWriter();
		final Marshaller marshaller = JAXB_CONTEXT_FACTORY
				.createMarshaller(obj.getClass(), additionalClasses);
		marshaller.marshal(obj, sw);
		return sw.toString();
	}

	/**
	 * This method unmarshalls an XML file into a JAXB object and takes the
	 * given charset in the XML preamble into account.
	 *
	 * @param klass
	 *            JAXB object class
	 * @param file
	 *            source file
	 * @return instance of passed JAXB object class
	 * @throws IOException
	 *             if file is not found or could not be closed
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static <T> T unmarshal(final Class<T> klass, final File file) throws IOException, JAXBException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return unmarshal(klass, fis);
		}
	}

	/**
	 * This method unmarshalls an XML byte array into a JAXB object and takes
	 * the given charset in the XML preamble into account.
	 *
	 * @param klass
	 *            JAXB object class
	 * @param rawXML
	 *            XML byte array
	 * @return instance of passed JAXB object class
	 * @throws IOException
	 *             if file is not found or could not be closed
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static <T> T unmarshal(final Class<T> klass, final byte[] rawXML) throws IOException, JAXBException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(rawXML)) {
			return unmarshal(klass, bais);
		}
	}

	/**
	 * This method unmarshalls a castor object and takes the given charset in
	 * the XML preamble into account.
	 *
	 * @param klass
	 *            JAXB object class
	 * @param bais
	 *            source input stream
	 * @return instance of passed JAXB object class
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static <T> T unmarshal(final Class<T> klass, final InputStream bais) throws JAXBException {
		final InputSource is = new InputSource(bais);
		return unmarshal(klass, is);
	}

	/**
	 * This method unmarshalls a JAXB object from an XML document string.
	 *
	 * @param klass
	 *            JAXB root class
	 * @param xmlStr
	 *            XML document in a string (NOT the name of an XML file!)
	 * @return instance of passed JAXB root class
	 * @throws JAXBException
	 *             on exceptions from JAXB API calls
	 */
	public static <T> T unmarshal(final Class<T> klass, final String xmlStr) throws JAXBException {
		final StringReader reader = new StringReader(xmlStr);
		return unmarshal(klass, new InputSource(reader));
	}

	@SuppressWarnings("unchecked")
	private static <T> T unmarshal(final Class<T> klass, final InputSource is) throws JAXBException {
		final Unmarshaller unmarshaller = JAXB_CONTEXT_FACTORY.createUnmarshaller(klass);
		return (T) unmarshaller.unmarshal(is);
	}
}
