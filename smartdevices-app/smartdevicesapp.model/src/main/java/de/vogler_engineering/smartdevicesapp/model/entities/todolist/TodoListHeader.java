/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import lombok.Data;

@Data
public class TodoListHeader {
    private String id;
    private String name;
    private String description;

    public TodoListHeader() {
    }

    public TodoListHeader(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public TodoListHeader(TodoListHeader other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
    }
}
