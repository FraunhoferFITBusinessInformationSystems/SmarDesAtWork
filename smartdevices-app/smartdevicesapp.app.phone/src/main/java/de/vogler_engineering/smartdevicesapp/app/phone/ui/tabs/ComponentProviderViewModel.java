/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs;

import androidx.databinding.ObservableList;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentDataProvider;

public abstract class ComponentProviderViewModel extends AbstractViewModel implements ComponentDataProvider {

    public abstract ComponentDataProvider getComponentDataProvider();

    public abstract ObservableList<UiComponent> getItems();
}
