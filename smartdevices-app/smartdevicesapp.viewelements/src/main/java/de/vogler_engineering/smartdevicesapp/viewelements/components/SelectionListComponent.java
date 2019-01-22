/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class SelectionListComponent extends BaseComponent<Object> {

    public SelectionListComponent(ComponentType type) {
        super(type);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        return null;
    }

    @Override
    public void bindView(Context context, ComponentData<Object> componentData, LifecycleOwner lifecycleOwner) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public View getView() {
        return null;
    }
}
