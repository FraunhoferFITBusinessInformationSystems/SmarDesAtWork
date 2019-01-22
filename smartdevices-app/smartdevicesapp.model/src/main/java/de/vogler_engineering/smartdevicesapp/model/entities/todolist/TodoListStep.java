/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TodoListStep extends UiComponent{
    public static final int StepIdNoMoreSteps = -1;

    private int number;
    private String description;
    private String resource;

    @JsonIgnore
    private TodoListStepState state = TodoListStepState.Default;

    @JsonIgnore
    private boolean clickable = false;

    @JsonIgnore
    private TodoListOverview parent;

    public TodoListStep() {
    }

    public TodoListStep(int number, String description, String resource, TodoListStepState state, boolean clickable, TodoListOverview parent) {
        this.number = number;
        this.description = description;
        this.resource = resource;
        this.state = state;
        this.clickable = clickable;
        this.parent = parent;
    }

    public TodoListStep(TodoListStep other){
        super(other);
        this.number = other.number;
        this.description = other.description;
        this.resource = other.resource;
        this.state = other.state;
        this.clickable = other.clickable;
        this.parent = other.parent;
    }
}
