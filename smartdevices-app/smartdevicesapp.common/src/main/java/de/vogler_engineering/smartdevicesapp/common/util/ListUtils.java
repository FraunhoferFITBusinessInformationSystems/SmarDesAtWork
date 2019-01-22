/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.util;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import de.vogler_engineering.smartdevicesapp.common.misc.Function;

/**
 * Created by vh on 25.02.2018.
 */

public class ListUtils {

    public static <T, U> T getItem(Collection<T> list, Function<T, U> selector, U search){
        for (T t : list) {
            if(selector.apply(t).equals(search))
                return t;
        }
        return null;
    }

    public static <T, U> boolean contains(Collection<T> list, Function<T, U> selector, U search){
        for (T t : list) {
            if(selector.apply(t).equals(search))
                return true;
        }
        return false;
    }

    public static <T, TPrimaryKey> List<T> getOrphanedItems(
            Collection<T> oldList,
            Collection<T> newList,
            Function<T, TPrimaryKey> keySelector){

        List<T> orphaned = new ArrayList<>();
        for (T t : oldList) {
            if(!contains(newList, keySelector, keySelector.apply(t))){
                orphaned.add(t);
            }
        }
        return orphaned;
    }

    public static <T, TPrimaryKey> Pair<List<T>, List<T>> getNewAndModifiedItems(
            Collection<T> oldList,
            Collection<T> newList,
            Function<T, TPrimaryKey> keySelector) {
        List<T> newItems = new ArrayList<>();
        List<T> modifiedItems = new ArrayList<>();
        for (T t : newList) {
            if(contains(oldList, keySelector, keySelector.apply(t)))
                modifiedItems.add(t);
            else
                newItems.add(t);
        }
        return new Pair<>(newItems, modifiedItems);
    }

    public static <T> void mergeInto(List<T> newList, List<T> oldList)
    {
        mergeInto(newList, oldList, null);
    }

    public static <T> void mergeInto(List<T> newList, List<T> oldList, Comparator<T> comparator)
    {
        if(oldList == null)
            throw new IllegalArgumentException("oldList should not be null");
        if(newList == null){
            oldList.clear();
            return;
        }
        int num = oldList.size() - newList.size();
        if (num > 0) {
            for (int j = 0; j < num; j++) {
                oldList.remove(oldList.size() - 1);
            }
        }
        for (int i = 0; i < newList.size(); i++) {
            if (i < oldList.size()) {
                if(comparator == null){
                    oldList.set(i, newList.get(i));
                }else{
                    if(comparator.compare(oldList.get(i), newList.get(i)) != 0) {
                        oldList.set(i, newList.get(i));
                    }
                }
            } else {
                oldList.add(newList.get(i));
            }
        }
    }

}
