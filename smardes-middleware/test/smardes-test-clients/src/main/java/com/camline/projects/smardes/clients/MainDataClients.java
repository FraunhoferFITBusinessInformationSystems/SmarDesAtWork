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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.maindata.api.MainDataFileRequest;
import com.camline.projects.smardes.maindata.api.MainDataQueryRequest;

public class MainDataClients extends AbstractClients {

	public MainDataClients() throws NamingException, IOException {
		super("MainData", true);
	}

	@Override
	protected boolean mainMenu() throws IOException, JMSException {
		System.out.println("\nMainData Clients");
		System.out.println("---------------------------------");
		System.out.println("1 - MainDataQuery");
		System.out.println("2 - MainDataFile");
		System.out.println("---------------------------------");
		System.out.println("X - Exit");

		final String command = StringUtils.defaultString(in.readString("Command"));

		switch (command.toLowerCase()) {
		case "1":
			doMainDataQuery();
			return true;
		case "2":
			doMainDataFile();
			return true;
		case "x":
			return false;
		default:
			return true;
		}
	}

	private void doMainDataQuery() throws IOException, JMSException {
		final String queryName = in.readString("Query Name");
		final int numNamed = in.readInt("Named Parameters");
		final int numPositional = in.readInt("Positional Parameters");

		final Map<String, Object> namedParams = new HashMap<>();
		for (int i = 0; i < numNamed; i++) {
			final Integer idx = Integer.valueOf(i+1);
			final String name = in.readString(String.format("Named Param Name %d", idx));
			final String value = in.readString(String.format("Named Param Value %d", idx));
			namedParams.put(name, value);
		}

		final List<Object> positionalParams = new ArrayList<>(numPositional);
		for (int i = 0; i < numPositional; i++) {
			final Integer idx = Integer.valueOf(i+1);
			final String value = in.readString(String.format("Positional Param Value %d", idx));
			positionalParams.add(value);
		}

		final MainDataQueryRequest request = new MainDataQueryRequest(queryName, namedParams, positionalParams);
		sendReceiveMessage(request);
	}

	private void doMainDataFile() throws IOException, JMSException {
		final int numFolders = in.readInt("Folders");

		final List<String> folders = new ArrayList<>(numFolders);
		for (int i = 1; i <= numFolders; i++) {
			final String value = in.readString(String.format("Folder %d", Integer.valueOf(i)));
			folders.add(value);
		}

		final String filename = in.readString("Filename");

		final MainDataFileRequest request = new MainDataFileRequest(folders, filename);
		sendReceiveMessage(request);
	}

	public static void main(final String[] args) throws NamingException, IOException {
		final MainDataClients specialClient = new MainDataClients();
		specialClient.execute();

		System.out.println("Exiting...");
	}
}
