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
package com.camline.projects.smardes.maindata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.camline.projects.smardes.common.br.BRCallable;
import com.camline.projects.smardes.common.br.IBR;
import com.camline.projects.smardes.jsonapi.ErrorResponse;

public class MainDataFileBR implements BRCallable<Object> {
	private final List<String> folders;
	private final String fileName;

	public MainDataFileBR(final List<String> folders, final String fileName) {
		this.folders = folders != null ? folders : Collections.emptyList();
		this.fileName = fileName;
	}

	@Override
	public List<String> getUnits() {
		return Arrays.asList(MainDataService.PERSISTENCE_UNIT);
	}

	@Override
	public Object call(final IBR br) {
		final String[] pathComponents = new String[folders.size() + 1];
		for (int i = 0; i < folders.size(); i++) {
			pathComponents[i] = folders.get(i);
		}
		pathComponents[folders.size()] = fileName;

		final Path path = Paths.get(MainDataConfig.instance().getRawFolder(), pathComponents);
		byte[] body;
		try {
			body = Files.readAllBytes(path);
		} catch (final IOException e) {
			return new ErrorResponse(MainDataErrors.MODULE, MainDataErrors.IO_ERROR, "Cannot read file " + path, e);
		}

		return body;
	}
}
