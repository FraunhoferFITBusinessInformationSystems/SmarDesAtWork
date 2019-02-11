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
package com.camline.projects.smardes.dbmon;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.GenericGenerator;

import com.camline.projects.smardes.jsonapi.SmarDesException;

@NamedQuery(
		name = LastEnd.NQ_FIND_BY_NAME,
		query = "select l from LastEnd l " +
				"where l.name = :" + LastEnd.NQP_NAME
)

@Entity
@Table(name = "SMARDES_LASTEND")
public class LastEnd {
	public static final String NQ_FIND_BY_NAME = "LastEnd.findByName";
	public static final String NQP_NAME = "name";

    private UUID uuid;
	private String name;
	private Long lastId1;
	private Date lastDate1;
	private String lastString1;
	private Long lastId2;
	private Date lastDate2;
	private String lastString2;
	private Long lastId3;
	private Date lastDate3;
	private String lastString3;

	public LastEnd() {
		// empty constructor for JPA
	}

	/**
	 * Constructor for new entries. It is required to set at least lastId or lastDate
	 * @param name name for entry
	 */
	public LastEnd(final String name) {
		this.name = name;
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

	@Column(name = "NAME_", unique = true, nullable = false, length = 1000)
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "LASTID1_")
	public Long getLastId1() {
		return lastId1;
	}

	public void setLastId1(final Long lastId1) {
		this.lastId1 = lastId1;
	}

	@Column(name = "LASTDATE1_")
	public Date getLastDate1() {
		return lastDate1;
	}

	public void setLastDate1(final Date lastDate1) {
		this.lastDate1 = lastDate1;
	}

	@Column(name = "LASTSTRING1_", length = 1000)
	public String getLastString1() {
		return lastString1;
	}

	public void setLastString1(final String lastString1) {
		this.lastString1 = lastString1;
	}

	@Column(name = "LASTID2_")
	public Long getLastId2() {
		return lastId2;
	}

	public void setLastId2(final Long lastId2) {
		this.lastId2 = lastId2;
	}

	@Column(name = "LASTDATE2_")
	public Date getLastDate2() {
		return lastDate2;
	}

	public void setLastDate2(final Date lastDate2) {
		this.lastDate2 = lastDate2;
	}

	@Column(name = "LASTSTRING2_", length = 1000)
	public String getLastString2() {
		return lastString2;
	}

	public void setLastString2(final String lastString2) {
		this.lastString2 = lastString2;
	}

	@Column(name = "LASTID3_")
	public Long getLastId3() {
		return lastId3;
	}

	public void setLastId3(final Long lastId3) {
		this.lastId3 = lastId3;
	}

	@Column(name = "LASTDATE3_")
	public Date getLastDate3() {
		return lastDate3;
	}

	public void setLastDate3(final Date lastDate3) {
		this.lastDate3 = lastDate3;
	}

	@Column(name = "LASTSTRING3_", length = 1000)
	public String getLastString3() {
		return lastString3;
	}

	public void setLastString3(final String lastString3) {
		this.lastString3 = lastString3;
	}

	@Transient
	public Object getLastObject1() {
		return ObjectUtils.firstNonNull(lastId1, lastDate1, lastString1);
	}

	@Transient
	public Object getLastObject2() {
		return ObjectUtils.firstNonNull(lastId2, lastDate2, lastString2);
	}

	@Transient
	public Object getLastObject3() {
		return ObjectUtils.firstNonNull(lastId3, lastDate3, lastString3);
	}

	@Transient
	public List<Object> getLastObjects() {
		return Arrays.asList(getLastObject1(), getLastObject2(), getLastObject3());
	}

	@Transient
	public boolean isLastObjectAvailable() {
		return Stream.of(getLastObject1(), getLastObject2(), getLastObject3()).anyMatch(Objects::nonNull);
	}

	public void setLastObjects(final Object value1, final Object value2, final Object value3) {
		lastId1 = null;
		lastDate1 = null;
		lastString1 = null;
		if (value1 instanceof Number) {
			lastId1 = convertToLong((Number)value1);
		} else if (value1 instanceof Date) {
			lastDate1 = (Date) value1;
		} else if (value1 != null){
			throwUnsupportedValueException(value1);
		}

		lastId2 = null;
		lastDate2 = null;
		lastString2 = null;
		if (value2 instanceof Number) {
			lastId2 = convertToLong((Number)value2);
		} else if (value2 instanceof Date) {
			lastDate2 = (Date) value2;
		} else if (value2 != null){
			throwUnsupportedValueException(value2);
		}

		lastId3 = null;
		lastDate3 = null;
		lastString3 = null;
		if (value3 instanceof Number) {
			lastId3 = convertToLong((Number)value3);
		} else if (value3 instanceof Date) {
			lastDate3 = (Date) value3;
		} else if (value3 != null){
			throwUnsupportedValueException(value3);
		}
	}

	private static Long convertToLong(Number number) {
		if (number instanceof Long) {
			return (Long) number;
		}
		return Long.valueOf(number.longValue());
	}

	private static void throwUnsupportedValueException(final Object value) {
		throw new SmarDesException(String.format("Unsupported value %s of type %s", value,
				(value != null ? value.getClass() : "null")));
	}

	@Override
	public String toString() {
		return "LastEnd [uuid=" + uuid + ", name=" + name + ", lastObject1=" + getLastObject1() + ", lastObject2="
				+ getLastObject2() + ", lastObject3=" + getLastObject3() + "]";
	}


}
