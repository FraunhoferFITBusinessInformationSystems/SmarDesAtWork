/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class TodoListClosedStep {
    private UUID uuid;
    private int step;
    private Date closedAt;
    private String closedBy;
}
