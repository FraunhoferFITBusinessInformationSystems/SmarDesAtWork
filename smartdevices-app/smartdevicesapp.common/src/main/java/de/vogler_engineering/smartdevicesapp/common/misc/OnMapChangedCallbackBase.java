/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

import java.lang.ref.WeakReference;

public class OnMapChangedCallbackBase<R, K, V> implements ObservableMap.OnMapChangedCallback<ObservableMap<K, V>, K, V> {

    protected WeakReference<R> reference;

    public OnMapChangedCallbackBase(R ref) {
        this.reference = new WeakReference<>(ref);
    }

    protected boolean isReferenceSet(){
        return (reference != null && reference.get() != null);
    }

    protected R getReference() {
        if(reference == null)
            return null;
        return reference.get();
    }

    @Override
    public void onKeyChanged(ObservableMap<K, V> map, K key) {
    }

    @Override
    public void onKeyAdded(ObservableMap<K, V> map, K key, V newValue) {
    }

    @Override
    public void onKeyUpdated(ObservableMap<K, V> map, K key, V oldValue, V newValue) {
    }

    @Override
    public void onKeyRemoved(ObservableMap<K, V> map, K key, V oldValue) {
    }

    @Override
    public void onMapCleared(ObservableMap<K, V> map) {
    }
}
