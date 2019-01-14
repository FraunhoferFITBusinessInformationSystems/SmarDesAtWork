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
package com.camline.projects.smardes.todo.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.camline.projects.smardes.todo.api.dto.ToDoHeaderDTO;
import com.camline.projects.smardes.todo.api.dto.ToDoInstanceDTO;

@NamedQuery(
		name = ToDoInstance.NQ_FIND_RUNNING_INSTANCES,
		query = "select i from ToDoInstance i where i.closedAt is null and i.abortedAt is null")

@Entity
@Table(name = "SMARDES_TODOLISTINSTANCE")
public class ToDoInstance {
	static final String NQ_FIND_RUNNING_INSTANCES = "ToDoInstance.findRunningInstances";

	private UUID uuid;
    private String domain;
    private String definitionId;
    private Map<String, String> context;
	private Date startedAt;
	private String startedBy;
	private Date closedAt;
	private String closedBy;
	private Date abortedAt;
	private String abortedBy;

    public ToDoInstance() {
		// empty constructor for JPA
	}

	public ToDoInstance(String domain, String definitionId, String startedBy, Map<String, String> context) {
		this.domain = domain;
		this.definitionId = definitionId;
		this.context = context;
		start(startedBy);
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

	@Column(name = "DOMAIN_", nullable = false, length = 1000)
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Column(name = "DEFINITIONID_", nullable = false, length = 1000)
	public String getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	@ElementCollection
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(
			name = "SMARDES_TODOLISTINSTANCE_CONTEXT",
			joinColumns = @JoinColumn(name = "instance_id"),
			foreignKey = @ForeignKey(name = "FK_TODOLIST_CONTEXT_INSTANCE"))
	public Map<String, String> getContext() {
		return context;
	}

	public void setContext(Map<String, String> context) {
		this.context = context;
	}

	private void start(String startedBy_) {
		setStartedAt(new Date());
		setStartedBy(startedBy_);
	}

	@Column(name = "STARTEDAT_", nullable = false)
	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date started) {
		this.startedAt = started;
	}

	@Column(name = "STARTEDBY_", nullable = false, length = 1000)
	public String getStartedBy() {
		return startedBy;
	}

	public void setStartedBy(String startedBy) {
		this.startedBy = startedBy;
	}

	public void close(String closedBy_) {
		setClosedAt(new Date());
		setClosedBy(closedBy_);
	}

	@Column(name = "CLOSEDAT_")
	public Date getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Date closedAt) {
		this.closedAt = closedAt;
	}

	@Column(name = "CLOSEDBY_", length = 1000)
	public String getClosedBy() {
		return closedBy;
	}

	public void setClosedBy(String closedBy) {
		this.closedBy = closedBy;
	}

	public void abort(String abortedBy_) {
		setAbortedAt(new Date());
		setAbortedBy(abortedBy_);
	}

	@Column(name = "ABORTEDAT_")
	public Date getAbortedAt() {
		return abortedAt;
	}

	public void setAbortedAt(Date abortedAt) {
		this.abortedAt = abortedAt;
	}

	@Column(name = "ABORTEDBY_", length = 1000)
	public String getAbortedBy() {
		return abortedBy;
	}

	public void setAbortedBy(String abortedBy) {
		this.abortedBy = abortedBy;
	}

	public ToDoInstanceDTO toDTO(ToDoHeaderDTO header) {
		return new ToDoInstanceDTO(uuid, startedAt, startedBy, closedAt, closedBy, abortedAt, abortedBy,
				new HashMap<>(context), header);
	}
}
