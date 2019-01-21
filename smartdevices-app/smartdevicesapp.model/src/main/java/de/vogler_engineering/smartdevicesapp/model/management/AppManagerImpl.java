/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.management;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.vogler_engineering.smartdevicesapp.common.util.StringUtil;
import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by vh on 09.02.2018.
 */

@Singleton
public class AppManagerImpl implements AppManager, SharedPreferences.OnSharedPreferenceChangeListener {

    private final MutableLiveData<String> baseUrl = new MutableLiveData<>();
    private final MutableLiveData<DeviceId> deviceId = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();

    private final MutableLiveData<OnlineState> onlineState = new MutableLiveData<>();
    private final MutableLiveData<Long> serverPing = new MutableLiveData<>();
    private final MutableLiveData<Date> lastServerRequest = new MutableLiveData<>();

    private final Map<String, Context> activityContext = new HashMap<>();

    private final Context appContext;
    private boolean initialized = false;

    @Getter @Setter private boolean logRetrofit = false;
    @Getter @Setter private boolean debugMode = false;

    private Context mainContext;
    private Navigator navigator;

    private SharedPreferences sharedPreferences;

    @Inject
    public AppManagerImpl(Context appContext){
        this.appContext = appContext;

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onlineState.setValue(OnlineState.OFFLINE);
        baseUrl.setValue("");
        deviceId.setValue(new DeviceId(PreferenceUtils.PREF_DEF_USERNAME, PreferenceUtils.PREF_DEF_DEVICENAME));
        isUpdating.setValue(false);
    }

    @Override
    public String getBaseUrl() {
        return baseUrl.getValue();
    }

    public LiveData<String> getBaseUrlObservable() {
        return baseUrl;
    }

    @Override
    public String getUsername(){
        return (getDeviceId() != null) ? getDeviceId().getUsername() : null;
    }

    @Override
    public String getDevicename(){
        return (getDeviceId() != null) ? getDeviceId().getUsername() : null;
    }

    @Override
    public String getDeviceIdKey(){
        return (getDeviceId() != null) ? getDeviceId().getIdKey() : null;
    }

    public DeviceId getDeviceId() {
        return deviceId.getValue();
    }

    public LiveData<DeviceId> getDeviceIdObservable() {
        return deviceId;
    }

    @Override
    public void setMainContext(Context mainContext) {
        this.mainContext = mainContext;
        this.initialized = true;
    }

    @Override
    public Context getMainContext(){
        return mainContext;
    }

    @Override
    public Context getAppContext() {
        return appContext;
    }

    @Override
    public boolean updateSettings() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(appContext);

        //Check if settings aren't present:
        if(!PreferenceUtils.isPreferencesInitialized(pref)){
            PreferenceUtils.initializePreferences(pref);
        }

        return updateSettings(pref);
    }

    @Override
    public boolean updateSettings(SharedPreferences pref) {
        String address    = pref.getString(PreferenceUtils.PREF_KEY_SERVER_ADDRESS, PreferenceUtils.PREF_DEF_SERVER_ADDRESS );
        String port       = pref.getString(PreferenceUtils.PREF_KEY_SERVER_PORT   , PreferenceUtils.PREF_DEF_SERVER_PORT);
        String username   = pref.getString(PreferenceUtils.PREF_KEY_USERNAME      , PreferenceUtils.PREF_DEF_USERNAME       );
        String devicename = pref.getString(PreferenceUtils.PREF_KEY_DEVICENAME    , PreferenceUtils.PREF_DEF_DEVICENAME     );

        if(!address.contains("://")){
            address = "http://" + address;
        }

        return updateSettings(address, port, username, devicename);
    }

    @Override
    public boolean updateSettings(String address, String port, String username, String devicename){
        String url =  address + ":" + port;

        if(StringUtil.isNotNullOrEmpty(address) &&
                StringUtil.isNotNullOrEmpty(port) &&
                StringUtil.isValidUrl(url) &&
                StringUtil.isNotNullOrEmpty(devicename) &&
                StringUtil.isNotNullOrEmpty(username)){
            baseUrl.postValue(url);
            deviceId.postValue(new DeviceId(username, devicename));
            return true;
        }else{
            return false;
        }
    }

    public OnlineState getOnlineState(){
        return onlineState.getValue();
    }

    public LiveData<OnlineState> getOnlineStateObservable() {
        return onlineState;
    }

    public LiveData<Long> getServerPingObservable() {
        return serverPing;
    }

    public LiveData<Date> getLastServerRequestObservable() {
        return lastServerRequest;
    }

    public void setConnectionState(OnlineState onlineState, long pingMs, Date requestDate) {
        this.onlineState.postValue(onlineState);
        this.serverPing.postValue(pingMs);
        this.lastServerRequest.postValue(requestDate);
    }

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceUtils.PREF_KEY_SERVER_ADDRESS) || key.equals(PreferenceUtils.PREF_KEY_SERVER_PORT)) {
            String address = sharedPreferences.getString(PreferenceUtils.PREF_KEY_SERVER_ADDRESS, PreferenceUtils.PREF_DEF_SERVER_ADDRESS);
            String port = sharedPreferences.getString(PreferenceUtils.PREF_KEY_SERVER_PORT, PreferenceUtils.PREF_DEF_SERVER_PORT);
            String url =  address + ":" + port;

            if(StringUtil.isNotNullOrEmpty(address) &&
                    StringUtil.isNotNullOrEmpty(port) &&
                    StringUtil.isValidUrl(url)){
                this.baseUrl.postValue(url);
            }
        }
        if(key.equals(PreferenceUtils.PREF_KEY_USERNAME) || key.equals(PreferenceUtils.PREF_KEY_DEVICENAME)){
            String username = sharedPreferences.getString(PreferenceUtils.PREF_KEY_USERNAME, PreferenceUtils.PREF_DEF_USERNAME);
            String devicename = sharedPreferences.getString(PreferenceUtils.PREF_KEY_DEVICENAME, PreferenceUtils.PREF_DEF_DEVICENAME);

            if(StringUtil.isNotNullOrEmpty(username) &&
                    StringUtil.isNotNullOrEmpty(devicename)){
                deviceId.postValue(new DeviceId(username, devicename));
            }
        }
    }

    @Override
    public void setIsUpdating(Boolean updating){
        isUpdating.postValue(updating);
    }
    @Override
    public Boolean getIsUpdating() {
        return isUpdating.getValue();
    }
    @Override
    public LiveData<Boolean> getIsUpdatingObservable() {
        return isUpdating;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setActivityContext(Activity activity) {
        String name = activity.getClass().getName();
        activityContext.put(name, activity);
    }
}
