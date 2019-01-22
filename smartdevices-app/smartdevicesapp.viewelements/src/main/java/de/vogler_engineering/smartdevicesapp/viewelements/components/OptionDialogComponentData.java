/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

public class OptionDialogComponentData extends ComponentData<ArrayList<String>>{


    @Getter
    private String[] items = null;

    @Getter
    @Setter
    private String delimiter = null;

    public OptionDialogComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
    }

    @Override
    public void setResourceValue(String s) {
        this.value.postValue(new ArrayList<String>(Arrays.asList(s.split(delimiter))));
    }

    @Override
    public String getResourceValue() {
        String listString = "";
        if(this.value.getValue() == null){
            return listString;
        }
        for (String s : this.value.getValue()) {
            listString += s + ";";
        }
        if(listString == "") listString = "Auswahl";
        else{
            listString = listString.substring(0, listString.length() -1);
        }
        return listString;
    }

    public void updatePropertySettings(){
        final String itemsStr = getFromAdditionalProperties("items", String.class);
        this.items = itemsStr.split(delimiter);
    }


}
