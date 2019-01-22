/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.ui;

import de.vogler_engineering.smartdevicesapp.model.entities.value.ValueDataType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UiValueSpecification extends UiComponent {
    private ValueDataType DataType;
    private String DisplayType;
}
