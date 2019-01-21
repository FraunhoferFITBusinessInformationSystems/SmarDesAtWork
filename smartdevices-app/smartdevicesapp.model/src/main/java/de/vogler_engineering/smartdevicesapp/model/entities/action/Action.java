/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.action;

import lombok.Data;

/**
 * Created by vh on 14.02.2018.
 */

@Data
public class Action {

    /// <summary>
    /// Unique key of the Action
    /// </summary>
    private String name;

    /// <summary>
    /// Text of the Action
    /// </summary>
    private String text;

    /// <summary>
    /// Target job of this Action
    /// </summary>
    private String jobName;

}
