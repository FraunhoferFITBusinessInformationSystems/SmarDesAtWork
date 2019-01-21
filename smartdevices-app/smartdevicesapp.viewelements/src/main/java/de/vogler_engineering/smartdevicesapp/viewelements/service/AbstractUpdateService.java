/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.service;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

import java.util.Date;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.management.DataController;
import de.vogler_engineering.smartdevicesapp.model.management.ServiceState;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public abstract class AbstractUpdateService extends IntentService {

    private static final String TAG = "AbstractUpdateService";

    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    BaseServiceManager serviceManager;

    @Inject
    DataController dataController;

    @Inject
    UiMessageUtil messageUtil;

    @Inject
    AppManager appManager;

    @Inject
    SchedulersFacade schedulersFacade;

    public AbstractUpdateService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        Timber.tag(TAG).d("Service created!");
        serviceManager.setServiceState(ServiceState.CREATED);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();

        if (action == null)
            return;

        handleServiceAction(action);
    }

    protected void handleServiceAction(String action){
        try {
            Timber.tag(TAG).d("Service started!");
            appManager.setIsUpdating(true);
            getServiceManager().setServiceState(ServiceState.RUNNING);

            if (PreferenceUtils.GETCONFIGNOTIFICATION.equals(action)) {
                updateConfig();
            } else if (PreferenceUtils.GETDATANOTIFICATION.equals(action)) {
                updateData();
            } else {
                updateAll();
            }

            getServiceManager().setUpdatedAt(new Date());
            appManager.setIsUpdating(false);
            Timber.tag(TAG).i("Update successfully: Action %s", action);
        }
        catch (Exception e){
            Timber.tag(TAG).e(e, "Could not run Update Action %s!", action);
        }
    }

    protected void updateConfig() throws Exception {
        getMessageUtil().makeDebugToast("Updating Config");
        getDataController().updateConfiguration();
    }

    protected void updateData() throws Exception {
        getMessageUtil().makeDebugToast("Updating Data");
        getDataController().updateDataOnly();
    }

    protected void updateAll() throws Exception {
        getMessageUtil().makeDebugToast("Updating All");
        getDataController().updateConfiguration();
        getDataController().updateData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    protected BaseServiceManager getServiceManager() {
        return serviceManager;
    }

    protected DataController getDataController() {
        return dataController;
    }

    protected UiMessageUtil getMessageUtil() {
        return messageUtil;
    }
}
