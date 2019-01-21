/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.service;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.management.ServiceState;
import timber.log.Timber;

public class ServiceManager extends BaseServiceManager {

    private static final String TAG = "ServiceManager";

    @Inject
    public ServiceManager(AppManager appManager, SchedulersFacade schedulers) {
        super(appManager, schedulers);
    }

    public void manageServices() {
        if(serviceState.getValue() != ServiceState.RUNNING){
            schedulers.io().scheduleDirect(() -> {
                if (updateInterval > 0 && appManager.getDeviceIdKey() != null && appManager.getBaseUrl() != null) {
                    Timber.tag(TAG).i("IntentUpdateService started.");
                    Context context = appManager.getAppContext();

                    Intent serviceIntent = null;

                    if (polling) {
//                        serviceIntent = new Intent(context, IntentUpdateService.class);
                    } else {
                        serviceIntent = new Intent(context, UpdateService.class);
                    }
                    context.startService(serviceIntent);
                }
            });
        }
    }
}
