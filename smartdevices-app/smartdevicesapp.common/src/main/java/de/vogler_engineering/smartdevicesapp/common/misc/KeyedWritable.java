/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

public interface KeyedWritable<K, V> extends KeyedReadable<K, V> {
    void set(K key, V value);
}