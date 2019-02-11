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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

class ConsoleInput implements Closeable {
	private static final int FILL_DOTS = 30;
	private static final String DOTS = ".....................................................................";

	private final BufferedReader in;

	ConsoleInput() {
		this.in = new BufferedReader(new InputStreamReader(System.in));
	}

	boolean readBoolean(final String prompt) throws IOException {
		final String input = readString(prompt);
		switch (input.toLowerCase()) {
		case "1":
		case "y":
		case "yes":
			return true;

		case "0":
		case "n":
		case "no":
			return false;

		default:
			return Boolean.parseBoolean(input);
		}
	}

	int readInt(final String prompt) throws IOException {
		final String input = readString(prompt);
		return Integer.parseInt(input);
	}

	String readString(final String prompt) throws IOException {
		return readString(prompt, FILL_DOTS);
	}

	String readString(final String prompt, final int numFillDots) throws IOException {
		final int numDots = Math.max(numFillDots - prompt.length(), 0);
		System.out.print("\n" + prompt + DOTS.substring(0, numDots) + ": ");
		return in.readLine();
	}

	UUID readUUID(final String prompt) throws IOException {
		String value = readString(prompt);
		return UUID.fromString(value);
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
