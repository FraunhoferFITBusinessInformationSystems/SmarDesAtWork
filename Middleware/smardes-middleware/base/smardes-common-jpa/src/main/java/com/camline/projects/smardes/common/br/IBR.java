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

import javax.persistence.EntityManager;

public interface IBR {
	/**
	 * Add interesting BR context
	 * @param name context name
	 * @param value context string value
	 */
	void addContext(String name, Object value);

	/**
	 * Add interesting BR context
	 * @param name context name
	 * @param value context boolean value
	 */
	void addContext(String name, boolean value);

	/**
	 * Add interesting BR context
	 * @param name context name
	 * @param value context long value
	 */
	void addContext(String name, long value);

	/**
	 * Increment an interesting BR context value; create on demand
	 * @param name context name
	 * @param additional context long value to increment
	 */
	void incContext(String name, long additional);

	/**
	 * DAO factory method
	 * @param daoClass DAO class to be needed
	 * @return DAO instance
	 */
	<T> T createDAO(Class<T> daoClass);

	EntityManager getEntityManager();

	<T> T createDAO(Class<T> daoClass, String unit);

	EntityManager getEntityManager(String unit);
}
