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
package com.camline.projects.smardes.common.br;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;

import com.camline.projects.smardes.common.jpa.DAOFactory;
import com.camline.projects.smardes.jsonapi.SmarDesException;

public final class BR<R> extends BaseBR<R> {
	private final Deque<DAOFactory> daoFactories;
	private Map<String, EntityManager> ems;

	private BR(final List<String> units, final String brName, final Logger log) {
		super(brName, log);
		this.daoFactories = units.stream().map(DAOFactory::getDAOFactory).collect(Collectors.toCollection(LinkedList::new));
	}

	public static <R> R execute(final Logger logger, final BRCallable<R> callable) {
		return new BR<R>(callable.getUnits(), callable.getName(), logger).execute(callable);
	}

	public static boolean execute(final Logger logger, final BRBooleanCallable callable) {
		return new BR<Boolean>(callable.getUnits(), callable.getName(), logger).execute(callable);
	}

	public static int execute(final Logger logger, final BRIntCallable callable) {
		return new BR<Integer>(callable.getUnits(), callable.getName(), logger).execute(callable);
	}

	public static void execute(final Logger logger, final BRVoidCallable callable) {
		new BR<Void>(callable.getUnits(), callable.getName(), logger).execute(callable);
	}

	@Override
	protected void init() {
		ems = daoFactories.stream()
				.collect(Collectors.toMap(DAOFactory::getUnit, DAOFactory::startTransaction, (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));

		List<String> problematicUnits = ems.entrySet().stream().filter(entry -> entry.getValue() == null)
				.map(Entry::getKey).collect(Collectors.toList());
		if (!problematicUnits.isEmpty()) {
			throw new SmarDesException("Persistence layer not correctly initialized: " + problematicUnits);
		}
	}

	@Override
	protected void markSucceeded() {
		ems.values().forEach(EntityManager::flush);
		super.markSucceeded();
	}

	@Override
	protected void cleanup() {
		RuntimeException oneException = null;
		for (EntityManager em : ems.values()) {
			try {
				if (em != null) {
					DAOFactory.endTransaction(em, isSucceeded());
				}
			} catch (final RuntimeException e) {
				if (isSucceeded()) {
					if (oneException != null) {
						logger.warn("Multiple exceptions but we can thrown only one. Skip this one.", e);
					} else {
						oneException = e;
					}
				}
				logger.warn("endTransaction failed but not raised", e);
			}
		}
		if (oneException != null) {
			throw oneException;
		}
	}

	@Override
	public <T> T createDAO(final Class<T> daoClass) {
		return DAOFactory.createDAO(daoClass, getEntityManager());
	}

	@Override
	public <T> T createDAO(final Class<T> daoClass, String unit) {
		return DAOFactory.createDAO(daoClass, ems.get(unit));
	}

	@Override
	public EntityManager getEntityManager() {
		if (ems.size() != 1) {
			throw new SmarDesException("Requested THE EntityManager but we have " + ems.size());
		}
		return ems.values().iterator().next();
	}

	@Override
	public EntityManager getEntityManager(String unit) {
		return ems.get(unit);
	}
}
