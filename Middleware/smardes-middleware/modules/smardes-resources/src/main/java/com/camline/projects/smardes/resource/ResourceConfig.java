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
package com.camline.projects.smardes.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public final class ResourceConfig {
	private static final ResourceConfig instance = new ResourceConfig();

	private final String resourceDir;
	private final String filesDir;
	private final long keepSeconds;
	private final String address;

	private ResourceConfig() {
		Properties properties;
		try {
			properties = PropertyUtils.loadSmardesConfiguration("resources.properties");
		} catch (IOException e) {
			throw new SmarDesException("Problems with resources.properties", e);
		}

		resourceDir = PropertyUtils.getStringProperty(properties, "resources.storagedir");
		final File resourceDirFile = Paths.get(resourceDir).toFile();
		resourceDirFile.mkdirs();

		filesDir = PropertyUtils.getStringProperty(properties, "resources.filesdir");
		final File filesDirFile = Paths.get(filesDir).toFile();
		filesDirFile.mkdirs();

		keepSeconds = TimeUnit.HOURS.toSeconds(PropertyUtils.getIntegerProperty(properties, "resources.keepHours"));
		address = PropertyUtils.getStringProperty(properties, "resources.address");
	}

	public static ResourceConfig instance() {
		return instance;
	}

	public String getResourceDir() {
		return resourceDir;
	}

	public String getFilesDir() {
		return filesDir;
	}

	public long getKeepSeconds() {
		return keepSeconds;
	}

	public String getAddress() {
		return address;
	}
}
