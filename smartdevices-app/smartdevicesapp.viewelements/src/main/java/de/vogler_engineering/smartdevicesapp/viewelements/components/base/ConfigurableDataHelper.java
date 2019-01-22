/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;

/**
 * Created by vh on 23.03.2018.
 */

public class ConfigurableDataHelper {

    private final Map<String, ComponentData> data = new HashMap<>();
    private final UiLayout config;
    private final ConfigurableViewModelFeatures features;
    private final ComponentFactory factory;

    public ConfigurableDataHelper(UiLayout config, ConfigurableViewModelFeatures features, ComponentFactory factory) {
        this.config = config;
        this.features = features;
        this.factory = factory;
        buildViewModel();
    }

    private void buildViewModel() {
        for (UiComponent element : config.getElements()) {
            ComponentData viewModel = factory.getComponentData(element, features);
            data.put(element.getId(), viewModel);
            //TODO set default data or copy from old map entry (for reconfig purpose)
        }
    }

    public ComponentData getComponentData(String id) {
        return data.get(id);
    }

    public Map<String,String> collectResources() {
        return collectResources(new HashMap<String, String>());
    }

    public Map<String,String> collectResources(Map<String,String> res) {
        for (UiComponent component : config.getElements()) {
            final ComponentData d = data.get(component.getId());
            res.put(component.getId(), d.getResourceValue());
        }
        return res;
    }

    public void initData(Job job) {
        for(UiComponent component : config.getElements()) {
            final ComponentData d = data.get(component.getId());
            if(job != null &&
                    job.getResource() != null &&
                    job.getResource().containsKey(component.getId())){
                String value = job.getResource().get(component.getId());
                d.setResources(job.getResource());
                d.setResourceValue(value);
            }
        }
    }
}
