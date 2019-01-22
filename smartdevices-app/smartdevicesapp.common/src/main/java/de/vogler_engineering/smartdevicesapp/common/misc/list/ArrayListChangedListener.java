/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc.list;

import androidx.databinding.ObservableArrayList;

public class ArrayListChangedListener<T> extends ObservableArrayList.OnListChangedCallback<ObservableArrayList<T>> {

    @Override
    public void onChanged(ObservableArrayList<T> sender) {
    }

    @Override
    public void onItemRangeChanged(ObservableArrayList<T> sender, int positionStart, int itemCount) {
    }

    @Override
    public void onItemRangeInserted(ObservableArrayList<T> sender, int positionStart, int itemCount) {
    }

    @Override
    public void onItemRangeMoved(ObservableArrayList<T> sender, int fromPosition, int toPosition, int itemCount) {
    }

    @Override
    public void onItemRangeRemoved(ObservableArrayList<T> sender, int positionStart, int itemCount) {
    }
}