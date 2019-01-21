/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

/**
 * Created by vh on 12.02.2018.
 */

public class LiteEnumMap<K extends Enum<K>, V> implements KeyedWritable<K, V> {

    private final Object[] values;

    public LiteEnumMap(Class<K> keyClass) {
        values = new Object[keyClass.getEnumConstants().length];
    }

    @Override
    public void set(K key, V value) {
        values[key.ordinal()] = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) {
        return (V) values[key.ordinal()];
    }

}