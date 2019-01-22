/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import java.util.List;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValue;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueData;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueProperties;
import de.vogler_engineering.smartdevicesapp.model.entities.value.GenericValueData;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;

public class ValueMonitorComponentData extends ComponentData<GenericValueData> implements Injectable {

    public final static int SEVERITY_NORMAL = 0;
    public final static int SEVERITY_WARNING = 1;
    public final static int SEVERITY_ERROR = 2;

    @Inject
    DynamicValueRepository dynamicValueRepository;

    private DynamicValueData dvd;
    private DynamicValue dv;
    private DynamicValueProperties dvp;

    private Observer<List<DynamicValueData>> observer = dynamicValueDataList -> {
        if (dynamicValueDataList != null) {
            for (DynamicValueData dynamicValueData : dynamicValueDataList) {
                DynamicValue dynamicValue = dynamicValueData.getValue();
                if(dynamicValue != null && dynamicValue.getName().equals(this.component.getName())) {
                    if (dynamicValue.getVal() != null) {
                        DynamicValueProperties val = dynamicValue.getVal();
                        dvd = dynamicValueData;
                        dv = dynamicValue;
                        dvp = val;

                        valueUpdated();

                        value.postValue(dvd);
                    }
                }
            }
        }
    };

    private void valueUpdated() {
        //Actual Value
        if(dvp.getValue() != null)
            actualValue = dvp.getValue();
        else
            actualValue = "-";

        //Nominal Value
        Object valObj = dvp.getFromAdditionalProperties("SetPoint");
        if (valObj != null)
            nominalValue = String.valueOf(valObj);
        else
            nominalValue = "-";

        //Unit
        Object unitObj = dvp.getFromAdditionalProperties("Unit");
        if (unitObj != null)
            unit = String.valueOf(unitObj);
        else
            unit = "";

        //Name
        Object obj = dvp.getFromAdditionalProperties("Name");
        if(obj != null)
            name = String.valueOf(obj);
        else name = "";

        //Id
        obj = dvp.getFromAdditionalProperties("Key");
        if(obj != null)
            id = String.valueOf(obj);
        else id = component.getName();

        //Severity
        obj = dvp.getFromAdditionalProperties("Severity");
        if(obj != null) {
            String sev = String.valueOf(obj);
            if(sev.equalsIgnoreCase("Normal"))
                severity = SEVERITY_NORMAL;
            else if(sev.equalsIgnoreCase("Warning"))
                severity = SEVERITY_WARNING;
            else if(sev.equalsIgnoreCase("Error"))
                severity = SEVERITY_ERROR;
        }else severity = SEVERITY_NORMAL;

        //TODO Additional Info
    }

    private int severity = 0;
    private String name = "";
    private String id = component.getName();
    private String unit = "";
    private String nominalValue = "-";
    private String actualValue = "-";
    private String additionalInfo = null;

    public ValueMonitorComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
        value.postValue(null);
    }

    @Override
    public void setResourceValue(String s) {
    }

    @Override
    public String getResourceValue() {
        return null;
    }

    public String getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getNominalValue(){

        return nominalValue + " " + unit;
    }

    public String getActualValue() {
        return actualValue + " " + unit;
    }

    public String getInfoText() {
        return additionalInfo;
    }

    public boolean hasInfoText() {
        return additionalInfo != null;
    }

    public int getSeverity() {
        return severity;
    }

    public void enablePolling(LifecycleOwner lifecycleOwner) {
        if(dynamicValueRepository != null) {
            dynamicValueRepository.getDataObservable().observe(lifecycleOwner, observer);
            dynamicValueRepository.enablePolling();
            dynamicValueRepository.startPolling();
        }
    }

    /*



        final Map<String, Object> props = element.getAdditionalProperties();

        if (props.containsKey("minValue")) {
            mMinNameTextView.setText(String.format("Min: %s", props.get("minValue")));
        } else mMinNameTextView.setText("");

        if (props.containsKey("maxValue")) {
            mMaxNameTextView.setText(String.format("Max: %s", props.get("maxValue")));
        } else mMaxNameTextView.setText("");

        if (props.containsKey("unit")) {
            mPostfix = String.valueOf(props.get("unit"));
        }

        String initial = "0";
        if (props.containsKey("initValue")) {
            initial = String.valueOf(props.get("initValue"));
        }
        mValueNameTextView.setText(String.format("%s%s%s", mPrefix, initial, mPostfix));

        data.valueLiveData().observe((LifecycleOwner) context, (valueData) -> {
            String value;
            if (valueData == null) {
                value = "---";
            } else {
                value = String.valueOf(valueData.getValue());
            }
            mValueNameTextView.setText(String.format("%s%s%s", mPrefix, value, mPostfix));
        });

     */

}
