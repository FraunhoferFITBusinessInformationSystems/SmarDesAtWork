/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

import java.util.Map;

/**
 * Created by vh on 28.02.2018.
 */

public interface ObservableMap<K, V> extends Map<K, V> {

    void addOnMapChangedCallback(OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> callback);
    void removeOnMapChangedCallback(OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> callback);

    interface OnMapChangedCallback<T extends ObservableMap<K, V>, K, V> {

        void onKeyChanged(T map, K key);
        void onKeyAdded(T map, K key, V newValue);
        void onKeyUpdated(T map, K key, V oldValue, V newValue);
        void onKeyRemoved(T map, K key, V oldValue);

        void onMapCleared(T map);

    }
}