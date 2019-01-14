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
package com.camline.projects.smardes.todo;

import java.io.IOException;
import java.util.Properties;

import javax.jms.JMSContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.AbstractSDService;
import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.common.jpa.DAOFactory;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public class ToDoService extends AbstractSDService {
	private static final Logger logger = LoggerFactory.getLogger(ToDoService.class);

	static final String PERSISTENCE_UNIT = "smardes";

	public ToDoService(final JMSContext baseJMSContext) {
		super(baseJMSContext, true, false);
	}

	@Override
	public void startup() {
		Properties properties;
		try {
			properties = PropertyUtils.loadSmardesConfiguration("todo.properties");
		} catch (IOException e) {
			throw new SmarDesException("Problems with todo.properties", e);
		}

		final String address = PropertyUtils.getStringProperty(properties, "todo.address");

		logger.info("Test DB connection...");
		DAOFactory.getDAOFactory(PERSISTENCE_UNIT);

		ToDoServiceHandler handler = new ToDoServiceHandler(getServiceJMSContext());
		handler.createConsumer(address);
		addMessageHandler(handler);

		logger.info("ToDoServiceHandler now listens on address {} for requests.", address);
	}
}
