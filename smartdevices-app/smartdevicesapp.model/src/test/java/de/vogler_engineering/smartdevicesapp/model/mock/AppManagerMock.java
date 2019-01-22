/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.Navigator;
import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;

public class AppManagerMock implements AppManager {

    public final MutableLiveData<String> baseUrl = new MutableLiveData<>();
    public final MutableLiveData<DeviceId> deviceId = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();

    public final MutableLiveData<OnlineState> onlineState = new MutableLiveData<>();
    public final MutableLiveData<Long> serverPing = new MutableLiveData<>();
    public final MutableLiveData<Date> lastServerRequest = new MutableLiveData<>();

    public final Map<String, Context> activityContext = new HashMap<>();

    private Context appContext;
    private boolean initialized = false;
    public SharedPreferences sharedPrefs;

    @Override
    public String getUsername(){
        return (deviceId.getValue() != null) ? deviceId.getValue().getUsername() : null;
    }

    @Override
    public String getDevicename(){
        return (deviceId.getValue() != null) ? deviceId.getValue().getUsername() : null;
    }

    @Override
    public String getDeviceIdKey() {
        return (deviceId.getValue() != null) ? deviceId.getValue().getIdKey() : null;
    }

    @Override
    public DeviceId getDeviceId() {
        return deviceId.getValue();
    }

    @Override
    public LiveData<DeviceId> getDeviceIdObservable() {
        return deviceId;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl.getValue();
    }

    @Override
    public LiveData<String> getBaseUrlObservable() {
        return baseUrl;
    }

    @Override
    public void setMainContext(Context mainContext) {
        this.appContext = mainContext;
    }

    @Override
    public Context getMainContext() {
        return appContext;
    }

    @Override
    public Context getAppContext() {
        return appContext;
    }

    @Override
    public boolean updateSettings() {
        return false;
    }

    @Override
    public boolean updateSettings(SharedPreferences pref) {
        return false;
    }

    @Override
    public boolean updateSettings(String address, String port, String username, String devicename) {
        return false;
    }

    @Override
    public boolean isLogRetrofit() {
        return true;
    }

    @Override
    public boolean isDebugMode() {
        return false;
    }

    @Override
    public void setLogRetrofit(boolean logRetrofit) {
    }

    @Override
    public void setConnectionState(OnlineState onlineState, long pingMs, Date requestDate) {

    }

    @Override
    public void setIsUpdating(Boolean updating) {

    }

    @Override
    public Boolean getIsUpdating() {
        return null;
    }

    @Override
    public LiveData<Boolean> getIsUpdatingObservable() {
        return null;
    }

    @Override
    public void setNavigator(Navigator navigationController) {

    }

    @Override
    public Navigator getNavigator() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return sharedPrefs;
    }
}
