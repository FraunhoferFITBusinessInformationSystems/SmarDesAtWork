/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;

/**
 * Created by vh on 23.03.2018.
 */

public class ButtonComponentData extends ComponentData<Void> {

    public ButtonComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
    }

    @Override
    public void setResourceValue(String s) {
    }

    @Override
    public String getResourceValue() {
        return getFromAdditionalProperties("value", String.class);
    }

    public void onClick() {
        String action = this.getFromAdditionalProperties("onClick", String.class);
        onClick(action);
    }

    protected void onClick(String action){
        if(action == null)
            return;

        if(action.equalsIgnoreCase("SendJob")){
            features.sendJob();
        }else if(action.equalsIgnoreCase("SendJobReply")){
            String value = getFromAdditionalProperties("Action", String.class);
            features.sendJobReply(value);
        }else if(action.equalsIgnoreCase("RemoveJob")){
            features.removeJob();
        }
    }
}
