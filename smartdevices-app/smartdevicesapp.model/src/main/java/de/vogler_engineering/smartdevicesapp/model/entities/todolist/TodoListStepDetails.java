/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TodoListStepDetails extends TodoListStep {

    private UUID instanceId;
    private TodoListHeader header;
    private TodoListStep nextStep;
    private TodoListStep previousStep;

    private int stepCount;
    private int stepIdx;

    public TodoListStepDetails() {
    }

    public TodoListStepDetails(UUID instanceId, TodoListHeader header, TodoListStep nextStep, TodoListStep previousStep) {
        this.instanceId = instanceId;
        this.header = header;
        this.nextStep = nextStep;
        this.previousStep = previousStep;
    }

    public TodoListStepDetails(TodoListStep step) {
        super(step);
    }

    public TodoListStepDetails(TodoListStepDetails other) {
        super(other);
        this.instanceId = other.instanceId;
        this.header = other.header;
        this.nextStep = other.nextStep;
        this.previousStep = other.previousStep;
    }
}
