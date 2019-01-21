/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import lombok.Getter;

public class NumberInputComponentData extends ComponentData<Integer> {

    @Getter
    private String suffix = null;

    @Getter
    private int count = 100;

    @Getter
    private int interval = 1;

    public NumberInputComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
    }

    @Override
    public void setResourceValue(String s) {
        updatePropertySettings();

        int parsed = 0;
        try{
            parsed = Integer.parseInt(s);
        }catch (NumberFormatException ignored){
        }

        int value = parsed / interval;
        this.value.postValue(value);
    }

    @Override
    public String getResourceValue() {
        int value = getIntValue();
        return String.valueOf(value * interval);
    }

    public void updatePropertySettings(){

        final Integer countProp = getFromAdditionalProperties("count", Integer.class);
        if(countProp != null){
            this.count = countProp;
        }

        final String suffix = getFromAdditionalProperties("suffix", String.class);
        this.suffix = suffix;

        Integer intervalProp = getFromAdditionalProperties("interval", Integer.class);
        if(intervalProp != null) {
            int value = getIntValue() * interval;
            this.interval = intervalProp;
            this.value.postValue(value / interval);
        }
    }

    public int getIntValue() {
        return this.value.getValue() == null ? 0 : this.value.getValue();
    }

    public int getFormattedValue() {
        return getIntValue() * interval;
    }
}
