/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

/**
 * Created by vh on 12.02.2018.
 */

public class ViewModelFactory<V extends ViewModel> implements ViewModelProvider.Factory  {

    private final GenericFactory<V> factory;
    private final Class<V> clazz;

    public ViewModelFactory(Class<V> clazz, GenericFactory<V> factory) {
        this.clazz = clazz;
        this.factory = factory;
    }


    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(clazz)) {
            return (T) factory.create();
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

    public interface GenericFactory<T extends ViewModel> {
        T create();
    }
}
