/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.todolist;

import android.content.Context;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class TodoListInstanceHeaderDto {
    private UUID id;
    private TodoListHeader header;
    private Date startedAt;
    private String startedBy;
    private TodoListContextInfo context;
}
