/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import java.util.Date;

import lombok.Data;

@Data
public class TodoListContextInfo {
    private String notes;
    private String number;
    private String subject;
    private Date startedAt;
    private String startedBy;
}
