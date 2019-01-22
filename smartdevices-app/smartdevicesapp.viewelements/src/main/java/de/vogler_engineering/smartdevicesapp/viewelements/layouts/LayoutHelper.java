/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public interface LayoutHelper {
    void createViews(Context context, LayoutInflater inflater, ViewGroup parentView, LifecycleOwner owner);
    void bindViews(Context context, LayoutViewModelReference viewModel, LifecycleOwner owner);
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
