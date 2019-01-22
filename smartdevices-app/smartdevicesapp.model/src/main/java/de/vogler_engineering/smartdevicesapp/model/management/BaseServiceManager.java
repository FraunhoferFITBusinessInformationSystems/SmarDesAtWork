/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.management;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;

public abstract class BaseServiceManager {

    private static final String TAG = "BaseServiceManager";

    protected final AppManager appManager;
    protected final SchedulersFacade schedulers;

    protected final MutableLiveData<ServiceState> serviceState = new MutableLiveData<>();

    protected Date updatedAt = null;
    protected long updateInterval = 1000;
    protected long retryInterval = 10000;

    public boolean isPolling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    protected boolean polling = true;

    public BaseServiceManager(AppManager appManager, SchedulersFacade schedulers) {
        this.appManager = appManager;
        this.schedulers = schedulers;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Date getLastUpdateAt() {
        return updatedAt;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setServiceState(ServiceState serviceState){
        this.serviceState.postValue(serviceState);
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setConnectionState(OnlineState onlineState, long pingMs, Date requestDate) {
        appManager.setConnectionState(onlineState, pingMs, requestDate);
    }

    public ServiceState getServiceState() {
        return serviceState.getValue();
    }

    public LiveData<ServiceState> getServiceStateObservable() {
        return serviceState;
    }

    public abstract void manageServices();

//TODO!!!!!
//    public void manageServices() {
//        manageUpdateService();
//        //manageOtherServices();
//    }
//
//    private void manageUpdateService(){
//        if(serviceState.getValue() != ServiceState.RUNNING){
//            schedulers.io().scheduleDirect(() -> {
//                //TODO Check if service is runnable
//                if (updateInterval > 0 && appManager.getDeviceIdKey() != null && appManager.getBaseUrl() != null) {
//                    Timber.tag(TAG).i("IntentUpdateService started.");
//                    Context context = appManager.getMainContext();
//                    if (context != null) {
//                        Intent serviceIntent = new Intent(context, Serv.class);
//                        context.startService(serviceIntent);
//                    }
//                }
//            });
//        }
//    }
}
