/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import java.util.ArrayList;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.util.MapUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import timber.log.Timber;

public class ConfigurableAdapterViewModel<D, C extends UiComponent> implements ComponentDataProvider {

    private static final String TAG = "ConfigurableAdapterViewModel";

    private final ObservableList<C> components = new ObservableArrayList<>();
    private final ArrayList<ComponentData<D>> data = new ArrayList<>();
    private final ConfigurableViewModelFeatures features;
    private final ComponentFactory factory;

    private ComponentEqualizer<C> componentEqualizer  = (c1, c2) ->
            c1.getId().equals(c2.getId())
            && c1.getName().equals(c2.getName())
            && c1.getType().equals(c2.getType())
            && MapUtils.equals(c1.getAdditionalProperties(), c2.getAdditionalProperties());

    private DataComparator<D> dataComparator = (d1, d2) -> d1.equals(d2) ? 0 : -1;

    public ConfigurableAdapterViewModel(ConfigurableViewModelFeatures features, ComponentFactory factory) {
        this.features = features;
        this.factory = factory;
        //this.dataProvider = dataProvider;
    }

    @Override
    public ComponentData<D> getComponentData(int id) {
        if(id<0 || id>=data.size()) {
            return null;
        }
        return data.get(id);
    }

    public C getComponent(int id) {
        return components.get(id);
    }

    public ObservableList<C> getComponents() {
        return components;
    }

    public ComponentData<D> createComponentData(UiComponent component) {
        @SuppressWarnings("unchecked")
        ComponentData<D> data = factory.getComponentData(component, features);
        return data;
    }

    public void addComponent(int index, C component, D initialValue) {
        ComponentData<D> componentData = createComponentData(component);
        if (initialValue != null) {
            componentData.setValue(initialValue);
        }
        data.add(index, componentData);
        components.add(index, component);
    }

    public void removeComponent(int index) {
        if (hasIndex(index)) {
            data.remove(index);
            components.remove(index);
        }
    }

    public void updateComponent(int index, C newComponent) {
        components.set(index, newComponent);
    }

    public void clearAll() {
        components.clear();
        data.clear();
    }

    public void updateData(int index, D newValue) {
        if(index < 0 || data.size() <= index){
            Timber.tag(TAG).e("Unknown index to update");
            return;
        }
        data.get(index).setValue(newValue);
    }


    public int getIndex(C component) {
        for (int i = 0; i < components.size(); i++) {
            if (component == components.get(i))
                return i;
        }
        return -1;
    }

    public int getIndex(String key){
        for (int i = 0; i < components.size(); i++) {
            if (key.equals(components.get(i).getId()))
                return i;
        }
        return -1;
    }

    public int getIndex(UUID id){
        for (int i = 0; i < components.size(); i++) {
            if (id.toString().equals(components.get(i).getId()))
                return i;
        }
        return -1;
    }

    public boolean hasIndex(int index){
        if(index >= 0 && index < components.size()){
            return true;
        }
        return false;
    }

    public interface ComponentEqualizer<T> {
        boolean equals(T c1, T c2);
    }

    public interface DataComparator<T> {
        int compare(T t1, T t2);
    }

    public void setComponentEqualizer(ComponentEqualizer<C> componentEqualizer) {
        this.componentEqualizer = componentEqualizer;
    }

    public ComponentEqualizer<C> getComponentEqualizer() {
        return componentEqualizer;
    }

    public void setDataComparator(DataComparator<D> dataComparator) {
        this.dataComparator = dataComparator;
    }

    public DataComparator<D> getDataComparator() {
        return dataComparator;
    }

}
