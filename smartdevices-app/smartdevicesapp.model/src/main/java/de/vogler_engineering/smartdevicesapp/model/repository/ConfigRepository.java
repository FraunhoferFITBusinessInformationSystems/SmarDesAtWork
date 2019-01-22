/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import android.annotation.SuppressLint;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceConfig;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceInfo;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.ServerInfo;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.ConfigRestService;
import io.reactivex.Single;
import timber.log.Timber;

/**
 * Created by vh on 19.02.2018.
 */

public class ConfigRepository extends AbstractRepository {

    private static final String TAG = "ConfigRepository";

    private final RestServiceProvider restServiceProvider;

    private final MutableLiveData<DeviceConfig> deviceConfig = new MutableLiveData<>();
    private final MutableLiveData<DeviceInfo> deviceInfo = new MutableLiveData<>();
    private final MutableLiveData<ServerInfo> serverInfo = new MutableLiveData<>();

    @Inject
    public ConfigRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        super(appManager, schedulersFacade);
        this.restServiceProvider = restServiceProvider;
    }

    public Single<DeviceInfo> updateDeviceInfo(){
        return getConfigRestService().getDeviceInfo(appManager.getDeviceIdKey())
                .map(newInfo -> {
                    deviceInfo.postValue(newInfo);
                    Timber.tag(TAG).d("DeviceInfo updated!");
                    return newInfo;
                });
    }

    public Single<DeviceConfig> updateDeviceConfig(){
        return getConfigRestService().getDeviceConfig(appManager.getDeviceIdKey())
                .map(newConfig -> {
                    deviceConfig.postValue(newConfig);
                    Timber.tag(TAG).d("DeviceConfig updated!");
                    return newConfig;
                });
    }

    public Single<ServerInfo> updateServerInfo(){
        ConfigRestService configRestService = getConfigRestService();
        Single<ServerInfo> serverInfo = configRestService.getServerInfo();
        return serverInfo
                .map(newInfo -> {
                    this.serverInfo.postValue(newInfo);
                    Timber.tag(TAG).d("ServerInfo updated!");
                    return newInfo;
                });
    }

    public Single<DeviceInfo> putDeviceInfo(DeviceInfo deviceInfo) {
        return getConfigRestService().putDeviceInfo(appManager.getDeviceIdKey(), deviceInfo)
                .subscribeOn(schedulersFacade.newThread())
                .map(newDeviceInfo -> {
                    this.deviceInfo.postValue(newDeviceInfo);
                    Timber.tag(TAG).d("DeviceInfo Pushed and Updated");
                    return newDeviceInfo;
                });
    }

    private ConfigRestService getConfigRestService(){
        return restServiceProvider.createRestService(ConfigRestService.class);
    }

    @SuppressLint("CheckResult")
    @Override
    public void updateConfig() {
        updateServerInfo().blockingGet();
        updateDeviceInfo().blockingGet();
        updateDeviceConfig().blockingGet();
    }

    public DeviceConfig getDeviceConfig() {
        return deviceConfig.getValue();
    }

    public LiveData<DeviceConfig> getDeviceConfigObservable(){
        return deviceConfig;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo.getValue();
    }

    public LiveData<DeviceInfo> getDeviceInfoObservable(){
        return deviceInfo;
    }

    public ServerInfo getServerInfo() {
        return serverInfo.getValue();
    }

    public LiveData<ServerInfo> getServerInfoObservable(){
        return serverInfo;
    }
}
