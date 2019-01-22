/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;

/**
 * Created by vh on 23.03.2018.
 */

public class DoubleButtonComponentData extends ButtonComponentData {

    private String value = null;

    public DoubleButtonComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
    }

    @Override
    public void setResourceValue(String s) {
    }

    @Override
    public String getResourceValue() {
        return value;
    }

    public void onClickPrimary() {
        String action = getFromAdditionalProperties("onClickPrimary", String.class);
        value = getFromAdditionalProperties("value", String.class);
        onClick(action);
    }

    public void onClickSecondary() {
        String action = getFromAdditionalProperties("onClickSecondary", String.class);
        value = getFromAdditionalProperties("valueSecondary", String.class);
        onClick(action);
    }
}
