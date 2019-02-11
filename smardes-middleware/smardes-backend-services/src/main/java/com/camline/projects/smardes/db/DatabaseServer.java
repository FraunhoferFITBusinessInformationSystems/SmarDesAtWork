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
package com.camline.projects.smardes.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.hsqldb.Database;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.hsqldb.server.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.camline.projects.smardes.common.SDService;
import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.common.logging.LoggerWriter;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public class DatabaseServer implements SDService {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseServer.class);
	private Server server;

	@Override
	public void startup() {
		server = new Server();
		server.setLogWriter(createLoggerWriter(Level.INFO));
		server.setErrWriter(createLoggerWriter(Level.ERROR));

		Properties props;
		try {
			props = PropertyUtils.loadExternalConfiguration("hsqldb.properties");
		} catch (IOException e) {
			throw new SmarDesException("Problems with hsqldb.properties", e);
		}
		logger.info("hsqlsb properties:\n{}", props);
		try {
			server.setProperties(new HsqlProperties(props));
		} catch (IOException | AclFormatException e) {
			throw new SmarDesException("Problems with setting HSQLDB server properties", e);
		}

		server.start();

		if (server.getState() != ServerConstants.SERVER_STATE_ONLINE) {
			throw new SmarDesException("Internal database server could not be started.");
		}
		logger.info("Internal database server started.");
	}

	private static PrintWriter createLoggerWriter(Level level) {
		return new LoggerWriter(logger, level);
	}

	@Override
	public void shutdown() {
		server.shutdownWithCatalogs(Database.CLOSEMODE_NORMAL);

		try (PrintWriter pw = server.getLogWriter()) {
			server.setLogWriter(null);
		}
		try (PrintWriter pw = server.getErrWriter()) {
			server.setErrWriter(null);
		}

		logger.info("Internal database server shut down.");
	}

	public static void main(final String[] args) {
		new DatabaseServer().startup();
	}
}
