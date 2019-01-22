/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import java.text.DateFormat;
import java.util.Date;

import de.vogler_engineering.smartdevicesapp.common.util.DateUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import timber.log.Timber;

public class DateViewComponentData extends ComponentData<Date> {

    private static final String TAG = "DateViewComponentData";

    private final DateFormat hrDateFormat = DateUtils.createHumanReadableDateFormat();
    private final DateFormat jsonDateFormat = DateUtils.createJsonDateFormat();

    public DateViewComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
    }

    @Override
    public void setResourceValue(String s) {
        try {
            Date d = jsonDateFormat.parse(s);
            value.postValue(d);
        }catch (Exception e){
            Timber.tag(TAG).e("Could not parse \"%s\" as json date. Component: %s.%s", s, component.getType(), component.getId());
        }
    }

    @Override
    public String getResourceValue() {
        if(value.getValue() == null) return null;
        return jsonDateFormat.format(value.getValue());
    }

    public String getValueFormatted(){
        if(value.getValue() == null) return "-";
        return hrDateFormat.format(value.getValue());
    }

}
