/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import de.vogler_engineering.smartdevicesapp.common.log.HuaweiDebugTree;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import timber.log.Timber;

/**
 * Created by vh on 08.03.2018.
 */

public abstract class SmartDevicesApplication extends Application implements HasActivityInjector, HasServiceInjector {

    @Inject
    AppManagerImpl appManager;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    protected void setupDebugLogger(){
        Timber.plant(new HuaweiDebugTree());
    }

    protected void setupNormalLogger(){
        Timber.plant(new Timber.DebugTree());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Setup the logger
        if(BuildConfig.DEBUG) {
            setupDebugLogger();
        }
    }

    protected AppManagerImpl getAppManager(){
        return appManager;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
