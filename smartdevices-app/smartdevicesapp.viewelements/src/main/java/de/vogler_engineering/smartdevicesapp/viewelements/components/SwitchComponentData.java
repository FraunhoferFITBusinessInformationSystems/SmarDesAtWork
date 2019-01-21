/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;

/**
 * Created by vh on 27.03.2018.
 */

public class SwitchComponentData extends ComponentData<Boolean> {

    public SwitchComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
        value.postValue(false);
    }

    @Override
    public void setResourceValue(String s) {
        try {
            boolean b = Boolean.parseBoolean(s);
            this.value.postValue(b);
        }
        catch(Exception ignored){ }
    }

    @Override
    public String getResourceValue() {
        return String.valueOf(value.getValue());
    }
}
