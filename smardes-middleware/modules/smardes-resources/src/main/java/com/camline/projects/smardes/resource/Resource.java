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

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@NamedQuery(
		name = Resource.NQ_LASTACCESSED_RECENTLY,
		query = "select r from Resource r " +
				"where r.lastAccessed > :" + Resource.NQP_RECENTLY
)

@Entity
@Table(name = "SMARDES_RESOURCE")
public class Resource {
	public static final String NQ_LASTACCESSED_RECENTLY = "Resource.lastAccessed.recently";
	public static final String NQP_RECENTLY = "recently";

    private UUID uuid;
	private String name;
	private String mimeType;
	private Date lastAccessed;

	public Resource() {
		// empty constructor for JPA
	}

	public Resource(final String name, final String mimeType) {
		this.name = name;
		this.mimeType = mimeType;
		this.lastAccessed = new Date();
	}

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "UUID_", columnDefinition = "BINARY(16)")
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(final UUID uuid) {
		this.uuid = uuid;
	}

	@Column(name = "NAME_", nullable = false, length = 1000)
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "MIMETYPE_", nullable = false, length = 100)
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	@Column(name = "LASTACCESSED_", nullable = false)
	public Date getLastAccessed() {
		return lastAccessed;
	}

	public void setLastAccessed(final Date lastAccessed) {
		this.lastAccessed = lastAccessed;
	}
}
