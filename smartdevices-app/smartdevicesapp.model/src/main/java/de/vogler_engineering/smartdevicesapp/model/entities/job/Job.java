/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.job;

import java.io.Serializable;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by vh on 14.02.2018.
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class Job extends Message implements Serializable {
    private JobStatus status;
    private boolean immediate;
    private Map<String, String> resource;
}
