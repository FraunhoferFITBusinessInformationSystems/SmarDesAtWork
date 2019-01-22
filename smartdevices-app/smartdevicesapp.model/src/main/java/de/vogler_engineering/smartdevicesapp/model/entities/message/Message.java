/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.message;

import java.util.Date;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.job.Priority;
import lombok.Data;

/**
 * Created by vh on 14.02.2018.
 */

@Data
public class Message {

    protected UUID id;
    protected UUID referenceId;
    protected Priority priority;

    private String name;

    protected Date createdAt;
    protected Date assignedAt;

    protected String createdBy;
    protected String assignedTo;

    protected String type;
}
