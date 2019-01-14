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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

public abstract class GenericDAO<T, ID extends Serializable> {

	private final Class<T> persistentClass;
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public GenericDAO() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private EntityManager getEntityManager() {
		if (entityManager == null) {
			throw new IllegalStateException(
					"EntityManager has not been set on DAO before usage");
		}
		return entityManager;
	}

	protected final Session getSession() {
		return (Session) getEntityManager().getDelegate();
	}

	protected final Class<T> getPersistentClass() {
		return persistentClass;
	}

	public void executeUpdate(final String statement) {
		getEntityManager().createNativeQuery(statement).executeUpdate();
	}

	public T findById(final ID id, final boolean lock) {
		if (!lock) {
			return getEntityManager().find(getPersistentClass(), id);
		}
		return getEntityManager().find(
				getPersistentClass(),
				id,
				LockModeType.PESSIMISTIC_WRITE);
	}

	public List<T> findAll(final String orderByProperty, final boolean ascending) {
		return findByCriteria(createOrder(orderByProperty, ascending));
	}

	public T merge(final T entity) {
		return getEntityManager().merge(entity);
	}

	public void remove(final T entity) {
		getEntityManager().remove(entity);
	}

	public void flush() {
		getEntityManager().flush();
	}

	public void clear() {
		getEntityManager().clear();
	}

	protected static final Order createOrder(final String propertyName, final boolean ascending) {
		if (propertyName == null) {
			return null;
		}
		return ascending ? Order.asc(propertyName) : Order.desc(propertyName);
	}

	/**
	 * Use this inside subclasses as a convenience method.
	 * @param criterion criterions
	 * @return result list
	 */
	@SuppressWarnings("unchecked")
	protected final List<T> findByCriteria(final Order order, final Criterion... criterion) {
		// TODO: replace Criteria and all other hibernate specific stuff
		final Criteria crit = getSession().createCriteria(getPersistentClass());
		for (final Criterion c : criterion) {
			crit.add(c);
		}
		if (order != null) {
			crit.addOrder(order);
		}
		return crit.list();
	}

	public int deleteAll() {
		final String hqlDelete = "delete " + persistentClass.getName();
		final Query deleteQuery = getEntityManager().createQuery(hqlDelete);
		return deleteQuery.executeUpdate();
	}

	public final Query createNamedQuery(final String id) {
		return getEntityManager().createNamedQuery(id);
	}

	final Query createNativeQuery(final String sql) {
		return getEntityManager().createNativeQuery(sql);
	}

	@SuppressWarnings("unchecked")
	protected List<T> getResultList(final Query query, final boolean lock) {
		if (lock) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	List<ID> getResultIDs(final Query query, final boolean lock) {
		if (lock) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	protected final T getSingleResultOrNull(final Query query, final boolean lock) {
		if (lock) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		try {
			return (T) query.getSingleResult();
		} catch (@SuppressWarnings("unused") final NoResultException e) {
			return null;
		}
	}
}
