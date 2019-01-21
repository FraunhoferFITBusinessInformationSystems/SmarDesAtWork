/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui;

import android.annotation.TargetApi;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import android.support.wearable.activity.WearableActivityDelegate;
import android.support.wearable.activity.WearableActivityDelegate.AmbientCallback;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import androidx.fragment.app.FragmentActivity;

/**
 * Base activity class for use on wearables. Provides compatibility for Ambient mode support.
 * <p>
 * Equals to {@link android.support.wearable.activity.WearableActivity} but uses
 * {@link AppCompatActivity} and {@link FragmentActivity} as Base classes.
 */
@TargetApi(21)
public abstract class AppCompatWearableActivity extends AppCompatActivity {
    private static final String TAG = "AppCompatWearableActivity";
    private boolean mSuperCalled;

    private final AmbientCallback callback = new AmbientCallback() {
        public void onEnterAmbient(Bundle ambientDetails) {
            AppCompatWearableActivity.this.mSuperCalled = false;
            AppCompatWearableActivity.this.onEnterAmbient(ambientDetails);
            if (!AppCompatWearableActivity.this.mSuperCalled) {
                String var2 = String.valueOf(AppCompatWearableActivity.this);
                Log.w("WearableActivity", (new StringBuilder(56 + String.valueOf(var2).length())).append("Activity ").append(var2).append(" did not call through to super.onEnterAmbient()").toString());
            }

        }

        public void onExitAmbient() {
            AppCompatWearableActivity.this.mSuperCalled = false;
            AppCompatWearableActivity.this.onExitAmbient();
            if (!AppCompatWearableActivity.this.mSuperCalled) {
                String var1 = String.valueOf(AppCompatWearableActivity.this);
                Log.w("WearableActivity", (new StringBuilder(55 + String.valueOf(var1).length())).append("Activity ").append(var1).append(" did not call through to super.onExitAmbient()").toString());
            }

        }

        public void onUpdateAmbient() {
            AppCompatWearableActivity.this.mSuperCalled = false;
            AppCompatWearableActivity.this.onUpdateAmbient();
            if (!AppCompatWearableActivity.this.mSuperCalled) {
                String var1 = String.valueOf(AppCompatWearableActivity.this);
                Log.w("WearableActivity", (new StringBuilder(57 + String.valueOf(var1).length())).append("Activity ").append(var1).append(" did not call through to super.onUpdateAmbient()").toString());
            }

        }
    };

    private final WearableActivityDelegate mDelegate;

    public AppCompatWearableActivity() {
        this.mDelegate = new WearableActivityDelegate(this.callback);
    }

    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDelegate.onCreate(this);
    }

    @CallSuper
    protected void onResume() {
        super.onResume();
        this.mDelegate.onResume();
    }

    @CallSuper
    protected void onPause() {
        this.mDelegate.onPause();
        super.onPause();
    }

    @CallSuper
    protected void onStop() {
        this.mDelegate.onStop();
        super.onStop();
    }

    @CallSuper
    protected void onDestroy() {
        this.mDelegate.onDestroy();
        super.onDestroy();
    }

    public final void setAmbientEnabled() {
        this.mDelegate.setAmbientEnabled();
    }

    public final void setAutoResumeEnabled(boolean enabled) {
        this.mDelegate.setAutoResumeEnabled(enabled);
    }

    public final void setAmbientOffloadEnabled(boolean enabled) {
        this.mDelegate.setAmbientOffloadEnabled(enabled);
    }

    public final boolean isAmbient() {
        return this.mDelegate.isAmbient();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mDelegate.dump(prefix, fd, writer, args);
    }

    @CallSuper
    public void onEnterAmbient(Bundle ambientDetails) {
        this.mSuperCalled = true;
    }

    @CallSuper
    public void onUpdateAmbient() {
        this.mSuperCalled = true;
    }

    @CallSuper
    public void onExitAmbient() {
        this.mSuperCalled = true;
    }
}
