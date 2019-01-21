/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import java.util.ArrayList;

import lombok.Data;

@Data
public class TodoListDetails {

    private TodoListHeader header;
    private ArrayList<TodoListStep> steps;

}
