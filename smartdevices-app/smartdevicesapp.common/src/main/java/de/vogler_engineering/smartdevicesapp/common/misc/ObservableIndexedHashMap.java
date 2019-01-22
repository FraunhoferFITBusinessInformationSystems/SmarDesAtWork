/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

public class ObservableIndexedHashMap<K, V> extends ObservableHashMap<K, V> {

    private transient final ArrayList<V> list = new ArrayList<>();

    public ObservableIndexedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ObservableIndexedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ObservableIndexedHashMap() {
    }

    public ObservableIndexedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public V put(K key, V value) {
        V old = super.put(key, value);
        list.add(old);
        return old;
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        //super.putAll(m);
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            put(key, value);
        }
    }

    @Override
    public V remove(Object key) {
        V old = super.remove(key);
        list.remove(old);
        return old;
    }

    @Override
    public void clear() {
        list.clear();
        super.clear();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(Object key, Object value) {
        boolean b = super.remove(key, value);
        list.remove(value);
        return b;
    }

    public ArrayList<V> getList() {
        return list;
    }

    public int indexOf(K key){
        V v = this.get(key);
        return list.indexOf(v);
    }

    public K getKey(int index){
        V v = getValue(index);
        for (K k : keySet()) {
            if(get(k) == v)
                return k;
        }
        return null;
    }

    public V getValue(int index){
        return list.get(index);
    }
}
