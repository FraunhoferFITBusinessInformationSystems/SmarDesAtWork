/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class TodoListInstance {

    private UUID id;
    private String todoListId;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, Boolean> stepState = new HashMap<>();

    private int currentStep;

    public TodoListInstance() {
    }

    public TodoListInstance(UUID id, String todoListId, Map<Integer, Boolean> stepState, int currentStep) {
        this.id = id;
        this.todoListId = todoListId;
        this.stepState = stepState;
        this.currentStep = currentStep;
    }

    public TodoListInstance(TodoListInstance other) {
        this.id = other.id;
        this.todoListId = other.todoListId;
        this.stepState = other.stepState;
        this.currentStep = other.currentStep;
    }
}
