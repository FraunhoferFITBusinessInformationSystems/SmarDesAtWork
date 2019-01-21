/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.vogler_engineering.smartdevicesapp.common.misc.Function;

/**
 * Created by vh on 01.03.2018.
 */

public class MapUtils {

    public static String getValue(Map<String, String> res, String key){
        return getValue(res, key, "");
    }

    public static String getValue(Map<String, String> res, String key, String defaultValue){
        if(res.containsKey(key)) return res.get(key);
        return defaultValue;
    }

    public static <K, T> void mergeListInMap(Map<K, T> map, Collection<T> list, Function<T, K> keySelector){
        if (list == null || list.size() == 0) {
            map.clear();
            return;
        }

        List<T> orphaned = ListUtils.getOrphanedItems(map.values(), list, keySelector);
        for (T o : orphaned) {
            map.remove(keySelector.apply(o));
        }

        for (T n : list) {
            map.put(keySelector.apply(n), n);
        }
    }

    public static <K, V> boolean equals(Map<K, V> a, Map<K, V> b) {
        if (a == b) return true;
        if (a.size() != b.size()) return false;

        try {
            for (Map.Entry<K, V> e : a.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(b.get(key) == null && b.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(b.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
        return true;
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        Set<T> keys = new HashSet<T>();
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }


    public static <T> T getKeyIgnoreCapitalCase(Map<String, T> map, String key) {
        String k = StringUtil.firstToLowerCase(key);
        if(map.containsKey(k)){
            return map.get(k);
        }
        k = StringUtil.firstToUpperCase(key);
        if(map.containsKey(k)){
            return map.get(k);
        }
        return null;
    }

    public static <K, V> Map<K, V> cloneMap(Map<K, V> source){
        return cloneMap(source, new HashMap<>());
    }

    public static <K, V> Map<K, V> cloneMap(Map<K, V> source, Map<K, V> destination){
        for (Map.Entry<K, V> entry : source.entrySet()) {
            destination.put(entry.getKey(), entry.getValue());
        }
        return destination;
    }

    public static <K, V> String printMap(Map<K, V> source){
        return printMap(source, Object::toString, Object::toString);
    }

    public static <K, V> String printMap(Map<K, V> source, Function<K, String> key2Str, Function<V, String> val2Str){
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<K, V> entry : source.entrySet()) {
            if(first) first = false;
            else sb.append(", ");
            sb.append('"').append(key2Str.apply(entry.getKey())).append("\":\"")
                    .append(val2Str.apply(entry.getValue())).append('"');
        }
        sb.append('}');
        return sb.toString();
    }
}
