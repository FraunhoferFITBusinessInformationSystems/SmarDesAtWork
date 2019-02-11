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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.br.BRVoidCallable;
import com.camline.projects.smardes.common.br.IBR;

final class ResourceGC implements BRVoidCallable {
	private static final Logger logger = LoggerFactory.getLogger(ResourceGC.class);

	private final File resourceDir;
	private final long keepSeconds;

	ResourceGC(final String resourceDir, final long keepSeconds) {
		this.resourceDir = new File(resourceDir);
		this.keepSeconds = keepSeconds;
	}

	@Override
	public List<String> getUnits() {
		return Arrays.asList(ResourceService.PERSISTENCE_UNIT);
	}


	@Override
	public void call(final IBR br) {
		final ResourceDAO dao = br.createDAO(ResourceDAO.class);
		final Date recently = new Date(System.currentTimeMillis() - 1000 * keepSeconds);
		final Map<String, Resource> allResources = dao.findAll(null, false).stream()
				.collect(Collectors.toMap(res -> res.getUuid().toString(), Function.identity()));

		final Counters counters = new Counters();
		Arrays.stream(resourceDir.listFiles()).filter(File::isFile)
				.forEach(file -> garbageCollect(dao, recently, allResources, counters, file));

		allResources.values().forEach(resource -> {
			logger.info("Resource {}/{} has no backed file. Deleting from DB...", resource.getUuid(), resource.getName());
			dao.remove(resource);
			counters.orphanDBEntry++;
		});

		br.addContext("deletedExpired", counters.expired);
		if (counters.orphanDBEntry > 0) {
			br.addContext("deletedOrphanDBEntry", counters.orphanDBEntry);
		}
		if (counters.orphanFile > 0) {
			br.addContext("deletedOrphanFile", counters.orphanFile);
		}
	}

	private static void garbageCollect(final ResourceDAO dao, final Date recently,
			final Map<String, Resource> allResources, final Counters counters, File file) {
		final String uuid = file.getName();
		final Resource resource = allResources.get(uuid);
		if (resource == null) {
			logger.info("File with UUID {} not found in database. Deleting...", uuid);
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				logger.warn("File " + file + " could not be deleted.", e);
			}
			counters.orphanFile++;
		} else if (resource.getLastAccessed().before(recently)) {
			logger.info("File {} accessed long ago: {}. Deleting...", uuid, resource.getLastAccessed());
			dao.remove(resource);
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				logger.warn("File " + file + " could not be deleted.", e);
			}
			counters.expired++;
		}
		allResources.remove(uuid);
	}

	static class Counters {
		int orphanFile;
		int orphanDBEntry;
		int expired;
	}
}
