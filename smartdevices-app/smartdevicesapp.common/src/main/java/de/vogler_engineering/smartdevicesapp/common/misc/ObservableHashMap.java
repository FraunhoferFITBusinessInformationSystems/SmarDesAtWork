/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by vh on 28.02.2018.
 */

@SuppressWarnings("WeakerAccess")
public class ObservableHashMap<K, V> extends HashMap<K, V> implements ObservableMap<K, V> {
    protected transient List<OnMapChangedCallback<ObservableHashMap<K, V>, K, V>> mListeners;

    public ObservableHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ObservableHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ObservableHashMap() {
    }

    public ObservableHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public V put(K key, V value) {
        V old = super.put(key, value);
        if(old != null) onKeyUpdated(key, old, value);
        else onKeyAdded(key, value);
        return old;
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        V old = super.remove(key);
        if(old != null) {
            onKeyRemoved((K) key, old);
        }
        return old;
    }

    @Override
    public void clear() {
        onMapCleared();
        super.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object key, Object value) {
        boolean b = super.remove(key, value);
        if(b) {
            onKeyRemoved((K) key, (V) value);
        }
        return b;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public V putIfAbsent(K key, V value) {
        return super.putIfAbsent(key, value);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public V replace(K key, V value) {
        return super.replace(key, value);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return super.computeIfAbsent(key, mappingFunction);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(key, remappingFunction);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.compute(key, remappingFunction);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(key, value, remappingFunction);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        super.replaceAll(function);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addOnMapChangedCallback(OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> callback) {
        if(mListeners == null)
            mListeners = new ArrayList<>();
        mListeners.add((OnMapChangedCallback<ObservableHashMap<K, V>, K, V>) callback);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeOnMapChangedCallback(OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> callback) {
        if(mListeners == null) return;
        mListeners.remove(callback);
    }

    private void onKeyAdded(K key, V newValue){
        if(mListeners == null) return;
        for(OnMapChangedCallback<ObservableHashMap<K, V>, K, V> listener : mListeners){
            listener.onKeyAdded(this, key, newValue);
        }
        onKeyChanged(key);
    }

    private void onKeyRemoved(K key, V oldValue){
        if(mListeners == null) return;
        for(OnMapChangedCallback<ObservableHashMap<K, V>, K, V> listener : mListeners){
            listener.onKeyRemoved(this, key, oldValue);
        }
        onKeyChanged(key);
    }

    private void onKeyUpdated(K key, V oldValue, V newValue){
        if(mListeners == null) return;
        for(OnMapChangedCallback<ObservableHashMap<K, V>, K, V> listener : mListeners){
            listener.onKeyUpdated(this, key, oldValue, newValue);
        }
        onKeyChanged(key);
    }

    private void onKeyChanged(K key) {
        if(mListeners == null) return;
        for(OnMapChangedCallback<ObservableHashMap<K, V>, K, V> listener : mListeners){
            listener.onKeyChanged(this, key);
        }
    }

    private void onMapCleared(){
        if(mListeners == null) return;
        for(OnMapChangedCallback<ObservableHashMap<K, V>, K, V> listener : mListeners){
            listener.onMapCleared(this);
        }
    }
}
