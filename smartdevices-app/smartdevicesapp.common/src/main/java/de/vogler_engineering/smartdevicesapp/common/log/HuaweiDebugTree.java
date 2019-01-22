/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.log;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import android.util.Log;

import timber.log.Timber;

public class HuaweiDebugTree extends Timber.Tree {
    @SuppressLint("LogNotTimber")
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if(priority == Log.VERBOSE || priority == Log.DEBUG)
            priority = Log.INFO;
        Log.println(priority, tag, message);
    }
}
