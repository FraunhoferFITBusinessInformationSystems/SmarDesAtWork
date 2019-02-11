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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;

import com.camline.projects.smardes.resources.api.GetFileRequest;
import com.camline.projects.smardes.resources.api.GetResourceRequest;
import com.camline.projects.smardes.resources.api.PutResourceRequest;

public class ResourceClients extends AbstractClients {

	public ResourceClients() throws NamingException, IOException {
		super("Resources", true);
	}

	@Override
	protected boolean mainMenu() throws IOException, JMSException {
		System.out.println("\nResource Clients");
		System.out.println("---------------------------------");
		System.out.println("1 - PutResource");
		System.out.println("2 - GetResource");
		System.out.println("3 - GetFile");
		System.out.println("---------------------------------");
		System.out.println("X - Exit");

		final String command = StringUtils.defaultString(in.readString("Command"));

		switch (command.toLowerCase()) {
		case "1":
			doPutResource();
			return true;
		case "2":
			doGetResource();
			return true;
		case "3":
			doGetFile();
			return true;
		case "x":
			return false;
		default:
			return true;
		}
	}

	private void doPutResource() throws JMSException, IOException {
		final String pathName = in.readString("Resource Path");
		final Path path = Paths.get(pathName);

		byte[] body;
		try {
			body = Files.readAllBytes(path);
		} catch (final IOException e) {
			System.err.println("Cannot read file " + path + ": " + e.getMessage());
			return;
		}

		final String mimeType = in.readString("Mime type");

		final PutResourceRequest request = new PutResourceRequest(path.getFileName().toString(), mimeType, body);
		sendReceiveMessage(request);
	}

	private void doGetResource() throws IOException, JMSException {
		final String uuid = in.readString("Resource UUID");

		final GetResourceRequest request = new GetResourceRequest(uuid);
		sendReceiveMessage(request);
	}

	private void doGetFile() throws IOException, JMSException {
		final String path = in.readString("Path");

		final GetFileRequest request = new GetFileRequest(path);
		sendReceiveMessage(request);
	}

	public static void main(final String[] args) throws NamingException, IOException {
		System.out.println("Resource Clients");
		System.out.println("------------------------\n");

		final ResourceClients client = new ResourceClients();
		client.execute();

		System.out.println("Exiting...");
	}
}
