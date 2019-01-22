/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;

/**
 * Creates
 */
public class JobViewLayoutHelper implements LayoutHelper{

    private final UiLayout layout;
    private final Map<String, View> views = new HashMap<>();
    private final Map<String, BaseComponent> helpers = new HashMap<>();

    @Inject
    ComponentFactory componentFactory;

    public JobViewLayoutHelper(UiLayout layout) {
        this.layout = layout;
    }

    @Override
    public void createViews(Context context, LayoutInflater inflater, ViewGroup parentView, LifecycleOwner owner) {

        for (UiComponent element : layout.getElements()) {
            BaseComponent comp = createComponent(element);
            comp.setElement(element);
            helpers.put(element.getId(), comp);
            View view = comp.createView(context, inflater);
            views.put(element.getId(), view);

            parentView.addView(view);
        }
    }

    @Override
    public void bindViews(Context context, LayoutViewModelReference viewModel, LifecycleOwner owner) {
        for (UiComponent component : layout.getElements()) {
            BaseComponent helper = helpers.get(component.getId());
            ComponentData componentData = viewModel.getDataHelper().getComponentData(component.getId());
            if(componentData == null) throw new IllegalArgumentException("No Component Data initialized!");
            //noinspection unchecked
            helper.bindView(context, componentData, owner);
        }
    }

    private BaseComponent createComponent(UiComponent element) {
        BaseComponent component = componentFactory.getComponent(element.getType());
        component.setElement(element);
        return component;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (UiComponent component : layout.getElements()) {
            BaseComponent helper = helpers.get(component.getId());
//            BaseComponent comp = createComponent(element);
            helper.onActivityResult(requestCode, resultCode, data);
        }
    }
}
