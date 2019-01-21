/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import de.vogler_engineering.smartdevicesapp.common.util.StringUtil;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;

/**
 * Created by vh on 22.03.2018.
 */

public abstract class ComponentData<T> {

    public final UiComponent component;
    protected final ConfigurableViewModelFeatures features;
    protected final MutableLiveData<T> value = new MutableLiveData<>();
    private Map<String, String> resources;

    public ComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        this.component = component;
        this.features = features;
    }

    public void setValue(T t){
        value.postValue(t);
    }

    public T getValue(){
        return value.getValue();
    }

    public LiveData<T> valueLiveData(){
        return value;
    }

    public abstract void setResourceValue(String s);

    public abstract String getResourceValue();

    protected Map<String, String> getResources(){
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public static <T> T getComponentData(ComponentData data, Class<T> clazz) {
        if(data != null && clazz.isInstance(data))
            return (T) data;
        return null;
    }

    public Object getFromAdditionalProperties(String key) {
        //Ignore fist key chase
        if(component == null ||
                component.getAdditionalProperties() == null) {
            return null;
        }
        String s = StringUtil.firstToLowerCase(key);
        if(component.getAdditionalProperties().containsKey(s)){
            return component.getAdditionalProperties().get(s);
        }
        s = StringUtil.firstToUpperCase(key);
        if(component.getAdditionalProperties().containsKey(s)){
            return component.getAdditionalProperties().get(s);
        }
        return null;
    }

    public <O> O getFromAdditionalProperties(String key, Class<O> clazz){
        Object o = getFromAdditionalProperties(key);
        if(o == null) return null;
        if(clazz.isInstance(o))
            //noinspection unchecked
            return (O)o;
        return null;
    }
}
