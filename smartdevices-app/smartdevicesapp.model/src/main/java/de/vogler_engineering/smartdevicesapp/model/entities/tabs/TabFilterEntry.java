/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.tabs;

import lombok.Data;

@Data
public class TabFilterEntry {

    private String key;
    private String name;
    private boolean invertable;
    private String filterType;

}
