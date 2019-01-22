/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc.list;

import androidx.databinding.ObservableArrayList;

import de.vogler_engineering.smartdevicesapp.common.misc.Consumer;

public class ArrayListCountChangedListener<T> extends ArrayListChangedListener<T> {

    private final Consumer<Integer> consumer;

    public ArrayListCountChangedListener(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onItemRangeInserted(ObservableArrayList<T> sender, int positionStart, int itemCount) {
        consumer.apply(sender.size());
    }

    @Override
    public void onItemRangeRemoved(ObservableArrayList<T> sender, int positionStart, int itemCount) {
        consumer.apply(sender.size());
    }
}
