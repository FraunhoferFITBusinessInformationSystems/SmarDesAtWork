/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.dto;

import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import lombok.Data;

@Data
public class TabEntryDto {

    private Job entry;
    private UiComponent listUi;

}
