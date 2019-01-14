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
package com.camline.projects.smardes.common.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.jsonapi.SmarDesException;

public final class DAOFactory {
	private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);

	private static final Map<String, DAOFactory> daoFactories = new HashMap<>();

	private final String unit;
	private EntityManagerFactory entityManagerFactory;

	private DAOFactory(final String unit) {
		this.unit = unit;
		try {
			logger.info("Initializing persistence unit {}", unit);
			entityManagerFactory = Persistence.createEntityManagerFactory(unit);
		} catch (final RuntimeException e) {
			logger.error("Could not create JPA entity manager factory for persistence unit " + unit + ". Disabling...", e);
		}
	}

	public String getUnit() {
		return unit;
	}

	public static synchronized DAOFactory getDAOFactory(final String unit) {
		DAOFactory daoFactory = daoFactories.get(unit);
		if (daoFactory == null) {
			daoFactory = new DAOFactory(unit);
			if (daoFactory.entityManagerFactory != null) {
				daoFactories.put(unit, daoFactory);
			}
		}
		return daoFactory;
	}

	public static synchronized void closeAll() {
		daoFactories.values().forEach(daoFactory -> daoFactory.entityManagerFactory.close());
	}

	public EntityManager startTransaction() {
		if (entityManagerFactory == null) {
			logger.warn("Cannot start transaction on uninitialized persistence unit '{}'", unit);
			return null;
		}

		final EntityManager em = entityManagerFactory.createEntityManager();

		final EntityTransaction tx = em.getTransaction();
		tx.begin();
		return em;
	}

	public static void endTransaction(final Object daoContext, final boolean successful) {
		final EntityManager entityManager = (EntityManager) daoContext;
		try {
			final EntityTransaction transaction = entityManager.getTransaction();
			if (!transaction.isActive()) {
				return;
			}

			if (!successful) {
				transaction.rollback();
				return;
			}

			boolean committed = false;
			try {
				transaction.commit();
				committed = true;
			} finally {
				if (!committed) {
					transaction.rollback();
				}
			}
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T createDAO(final Class<T> daoClass, final EntityManager em) {
		GenericDAO<?, ?> dao;
		try {
			dao = (GenericDAO<?, ?>) daoClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new SmarDesException("Unexpected problems in DAO creation", e);
		}
		dao.setEntityManager(em);
		return (T) dao;
	}
}
