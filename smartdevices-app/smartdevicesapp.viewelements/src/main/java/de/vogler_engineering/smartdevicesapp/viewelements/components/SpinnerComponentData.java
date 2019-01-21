/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import timber.log.Timber;

/**
 * Created by vh on 27.03.2018.
 */

public class SpinnerComponentData extends ComponentData<Integer> {

    private static final String TAG = "SpinnerComponentData";

    private final ArrayList<String> items = new ArrayList<>();
    private ArrayList<String> labels;
    private int defaultIndex;

    private boolean listFromConfig = true;

    public SpinnerComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);

        initComponent();
    }

    private void initComponent() {
        String str = getFromAdditionalProperties("items", String.class);
        if (str != null) {
            if(!parseAndSetList(str, items)){
                Timber.tag(TAG).w("Could not read ItemList");
            }
            defaultIndex = 0;
            value.postValue(defaultIndex);
        } else {
            listFromConfig = false;
            Timber.tag(TAG).i("Items not provided!");
        }
    }

    private boolean parseAndSetList(String string, List<String> list){
        String[] splits = string.split(";");
        if(splits.length == 0){
            return false;
        }

        list.clear();
        for (String split : splits) {
            split = split.trim();
            if (!split.isEmpty()) {
                list.add(split);
            }
        }
        return true;
    }

    @Override
    public void setResourceValue(String s) {
        if(listFromConfig) {
            //List entries already set from ui-configuration
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                if (item.equals(s)) {
                    value.postValue(i);
                    return;
                }
            }
            value.postValue(0);
        }else{
            //Setting list from JobResource
            Map<String, String> res = getResources();
            if(res != null){
                String key = component.getId()+"_values";
                if(res.containsKey(key)){
                    String v = res.get(key);
                    parseAndSetList(v, items);

                    //now set index to s
                    for (int i = 0; i < items.size(); i++) {
                        if(Objects.equals(s, items.get(i))){
                            defaultIndex = i;
                            value.postValue(defaultIndex);
                            break;
                        }
                    }
                }else{
                    parseAndSetList(s, items);
                    defaultIndex = 0;
                    value.postValue(defaultIndex);
                }
                key = component.getId()+"_labels";
                if(res.containsKey(key)){
                    String v = res.get(key);
                    if(labels == null) labels = new ArrayList<>();
                    parseAndSetList(v, labels);
                }
            }else{
                parseAndSetList(s, items);
                defaultIndex = 0;
                value.postValue(defaultIndex);
            }
        }
    }

    public ArrayList<String> getLabels() {
        if(labels != null &&
                !labels.isEmpty() &&
                labels.size() == items.size()){
            return labels;
        }
        return items;
    }

    @Override
    public String getResourceValue() {
        if(items.size() == 0) return null;
        return items.get(getIntValue());
    }

    public int getIntValue() {
        Integer val = super.getValue();
        return val == null ? 0 : val;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public int getDefaultIndex() {
        return defaultIndex;
    }
}
