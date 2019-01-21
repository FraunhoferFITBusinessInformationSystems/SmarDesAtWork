/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.job;

import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by vh on 25.02.2018.
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class JobReply extends Message{

    private String actionKey;
    private Map<String, String> resource;

}