/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.value.ValueData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;

public class ValueDisplayPlainComponentData extends ComponentData<ValueData> {



    public ValueDisplayPlainComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
        value.postValue(null);
    }

    @Override
    public void setResourceValue(String s) {
        ValueData vd = new ValueData();
        vd.setName("foo");
        vd.setValue("0");
        vd.setConfigKey("bar");
        this.value.setValue(new ValueData());

    }

    @Override
    public String getResourceValue() {
        return null;
    }
}
