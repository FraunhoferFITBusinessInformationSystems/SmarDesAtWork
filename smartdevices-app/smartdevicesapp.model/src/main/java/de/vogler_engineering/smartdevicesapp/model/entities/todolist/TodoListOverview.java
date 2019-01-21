/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TodoListOverview extends TodoListHeader {

    private UUID instanceId;

    private int currentStep = TodoListStep.StepIdNoMoreSteps;
    private int nextStep = TodoListStep.StepIdNoMoreSteps;
    private int previousStep = TodoListStep.StepIdNoMoreSteps;

    private List<TodoListStep> steps = new ArrayList<>();

    private TodoListContextInfo context;

    public TodoListOverview() {
    }

    public TodoListOverview(TodoListHeader other) {
        super(other);
    }

    public TodoListOverview(TodoListOverview other) {
        super(other);
        this.instanceId = other.instanceId;
        this.currentStep = other.currentStep;
        this.nextStep = other.nextStep;
        this.previousStep = other.previousStep;
        this.steps = other.steps;
        this.context = other.context;
    }
}
