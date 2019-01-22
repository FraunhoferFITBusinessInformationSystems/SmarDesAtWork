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
import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceInfo;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import timber.log.Timber;

public class DeviceInfoRepository extends AbstractRepository {

    private static final String TAG = "DeviceInfoRepository";

    private final ConfigRepository configRepository;

    private boolean deviceInfoChanged = true;

    private final MutableLiveData<DeviceInfo> deviceInfo = new MutableLiveData<>();

    @Inject
    public DeviceInfoRepository(AppManager appManager, SchedulersFacade schedulersFacade, ConfigRepository configRepository) {
        super(appManager, schedulersFacade);
        this.configRepository = configRepository;

        this.deviceInfo.postValue(new DeviceInfo());
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo.getValue();
    }

    public void updateDeviceInfo(DeviceInfo newInfo) {
        DeviceInfo di = deviceInfo.getValue();
        if(di == null){
            deviceInfo.postValue(newInfo);
            deviceInfoChanged = true;
            return;
        }
        if (newInfo.getDeviceName() != null) di.setDeviceName(newInfo.getDeviceName());
        if (newInfo.getFamily() != null) di.setFamily(newInfo.getFamily());
        if (newInfo.getFcmToken() != null) di.setFcmToken(newInfo.getFcmToken());
        if (newInfo.getType() != null) di.setType(newInfo.getType());
        for (String k : newInfo.getProperties().keySet()) {
            String v = newInfo.getProperties().get(k);
            if(v != null) {
                di.getProperties().put(k, v);
            }
        }
        //Reenque the old value to push update.
        deviceInfo.postValue(di);
        deviceInfoChanged = true;
    }

    public void setDeviceToken(String refreshedToken) {
        deviceInfo.getValue().setFcmToken(refreshedToken);
        deviceInfoChanged = true;
    }

    public LiveData<DeviceInfo> getDeviceInfoObservable() {
        return deviceInfo;
    }

    @SuppressLint("CheckResult")
    @Override
    public void updateData() {
        if (deviceInfoChanged) {
            DeviceInfo di = this.deviceInfo.getValue();
            deviceInfoChanged = false;
            configRepository.putDeviceInfo(di)
                    .observeOn(schedulersFacade.io())
                    .subscribe(this.deviceInfo::postValue,
                            t -> {
                                Timber.tag(TAG).e(t, "Could not update DeviceInfo!");
                                deviceInfoChanged = true;
                            });
        }
    }
}
