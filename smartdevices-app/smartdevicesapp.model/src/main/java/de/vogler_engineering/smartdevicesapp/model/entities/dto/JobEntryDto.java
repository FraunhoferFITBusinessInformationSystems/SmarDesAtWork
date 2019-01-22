/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.dto;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobEntryDto extends TabEntryDto {

    private UiLayout ui;

}
