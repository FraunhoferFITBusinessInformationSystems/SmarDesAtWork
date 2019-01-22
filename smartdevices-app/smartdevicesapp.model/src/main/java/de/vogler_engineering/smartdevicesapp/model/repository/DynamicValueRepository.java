/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueData;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.DataRestService;
import timber.log.Timber;

public class DynamicValueRepository extends AbstractRepository {

    private static final String TAG = "DynamicValueRepository";

    private final RestServiceProvider restServiceProvider;
    private long pollingInterval = 500;

    private final AtomicBoolean pollingActive = new AtomicBoolean(false);
    private final AtomicBoolean pollingEnabled = new AtomicBoolean(false);
    private final MutableLiveData<List<DynamicValueData>> data = new MutableLiveData<>();

    public DynamicValueRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        super(appManager, schedulersFacade);
        this.restServiceProvider = restServiceProvider;

    }

    public LiveData<List<DynamicValueData>> getDataObservable() {
        return data;
    }

    private DataRestService getDataRestService() {
        return restServiceProvider.createRestService(DataRestService.class);
    }

    private boolean makeUpdate() {
        List<DynamicValueData> dynamicValueData = getDataRestService()
                .getDynamicValueData(appManager.getDeviceIdKey())
                .blockingGet();
        if (dynamicValueData != null) {
            this.data.postValue(dynamicValueData);
            return true;
        }
        return false;
    }

    public void startPolling() {
        if (pollingActive.getAndSet(true)) {
            return;
        }

        //Start polling chain
        startUpdateTask();
    }

    public void stopPolling() {
        pollingActive.set(false);
    }

    private void startUpdateTask(){
        LoadDynamicValuesAsyncTask loadDynamicValuesAsyncTask = new LoadDynamicValuesAsyncTask();
        loadDynamicValuesAsyncTask.execute(this);
    }

    public boolean isPollingEnabled() {
        return pollingEnabled.get();
    }

    public void enablePolling() {
        pollingEnabled.set(true);
    }

    public void disablePolling(){
        pollingEnabled.set(false);
    }

    private static class LoadDynamicValuesAsyncTask extends AsyncTask<DynamicValueRepository, Void, Boolean> {

        @Override
        protected Boolean doInBackground(DynamicValueRepository... repos) {
            DynamicValueRepository repo = repos[0];

            try {
                boolean res = repo.makeUpdate();
                if (!res) { //ON error
                    throw new Exception("Update error");
                }
            }catch (Exception e){
                Timber.tag(TAG).e(e, "Error while polling Value!");
            }

            Timber.tag(TAG).d("Polling data updated!");

            try {
                Thread.sleep(repo.pollingInterval);
            } catch (InterruptedException e) {
                Timber.tag(TAG).d("Value polling interrupted.");
            }

            if(repo.pollingActive.get()){
                //Restart Update Task
                repo.startUpdateTask();
            }
            return true;
        }
    }

    public long getPollingIntervalMs() {
        return pollingInterval;
    }
}
