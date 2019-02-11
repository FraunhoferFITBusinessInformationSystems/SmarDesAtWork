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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.camline.projects.smardes.todo.api.dto.ToDoClosedStepDTO;

@NamedQuery(
		name = ToDoClosedStep.NQ_FIND_BY_INSTANCE,
		query = "select s from ToDoClosedStep s where s.instance = :" + ToDoClosedStep.NQP_INSTANCE)

@NamedQuery(
		name = ToDoClosedStep.NQ_FIND_BY_INSTANCE_STEP,
		query = "select s from ToDoClosedStep s where s.instance = :" + ToDoClosedStep.NQP_INSTANCE
				+ " and s.step = :" + ToDoClosedStep.NQP_STEP)

@Entity
@Table(name = "SMARDES_TODOLISTSTEP")
public class ToDoClosedStep {
	static final String NQ_FIND_BY_INSTANCE = "ToDoClosedStep.findByInstance";
	static final String NQ_FIND_BY_INSTANCE_STEP = "ToDoClosedStep.findByInstanceStep";
	static final String NQP_INSTANCE = "instance";
	static final String NQP_STEP = "step";

    private UUID uuid;
    private ToDoInstance instance;
    private int step;
	private Date closedAt;
	private String closedBy;

    public ToDoClosedStep() {
		// empty constructor for JPA
	}

	public ToDoClosedStep(ToDoInstance instance, int step, String processedBy) {
		this.instance = instance;
		this.step = step;
		this.closedAt = new Date();
		this.closedBy = processedBy;
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

	@ManyToOne
	@JoinColumn(name = "INSTANCE_", nullable = false,
			foreignKey = @ForeignKey(name = "FK_TODOLIST_STEP_INSTANCE"))
	public ToDoInstance getInstance() {
		return instance;
	}

	public void setInstance(ToDoInstance instance) {
		this.instance = instance;
	}

	@Column(name = "STEP_", nullable = false)
	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	@Column(name = "CLOSEDAT_", nullable = false)
	public Date getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Date processed) {
		this.closedAt = processed;
	}

	@Column(name = "CLOSEDBY_", nullable = false, length = 1000)
	public String getClosedBy() {
		return closedBy;
	}

	public void setClosedBy(String processedBy) {
		this.closedBy = processedBy;
	}

	public ToDoClosedStepDTO toDTO() {
		return new ToDoClosedStepDTO(uuid, step, closedAt, closedBy);
	}
}
