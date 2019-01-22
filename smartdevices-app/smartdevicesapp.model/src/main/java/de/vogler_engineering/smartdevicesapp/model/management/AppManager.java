/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.management;

import androidx.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;

public interface AppManager {

    DeviceId getDeviceId();
    LiveData<DeviceId> getDeviceIdObservable();

    String getBaseUrl();
    LiveData<String> getBaseUrlObservable();

    String getUsername();
    String getDevicename();
    String getDeviceIdKey();

    boolean isLogRetrofit();
    boolean isDebugMode();
    void setLogRetrofit(boolean logRetrofit);

    //Context
    void setMainContext(Context mainContext);
    Context getMainContext();
    Context getAppContext();

    /**
     * Gets the most specific Context that is available to the AppManager implementation.
     *
     * @return The most specific Context object.
     */
    default Context getContext(){
        Context ctx = getMainContext();
        if(ctx != null) return ctx;
        return getAppContext();
    }

    //Settings
    boolean updateSettings();
    boolean updateSettings(SharedPreferences pref);
    boolean updateSettings(String address, String port, String username, String devicename);

    void setConnectionState(OnlineState onlineState, long pingMs, Date requestDate);

    //Data Update State
    void setIsUpdating(Boolean updating);
    Boolean getIsUpdating();
    LiveData<Boolean> getIsUpdatingObservable();

    void setNavigator(Navigator navigationController);
    Navigator getNavigator();

    SharedPreferences getSharedPreferences();

}
