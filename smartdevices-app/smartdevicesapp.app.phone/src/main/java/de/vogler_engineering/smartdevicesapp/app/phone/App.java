/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone;

import com.google.firebase.FirebaseApp;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.app.phone.di.AppInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.service.ServiceManager;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.model.entities.enums.DeviceFamily;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.SmartDevicesApplication;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.DataUpdateNotificationBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DeviceInfoUtils;

/**
 * Created by vh on 09.02.2018.
 */

public class App extends SmartDevicesApplication {

    @Inject
    NavigationController navigationController;

    @Inject
    ServiceManager serviceManager;

    @Override
    public void onCreate() {
        super.onCreate();

        DeviceInfoUtils.setDeviceFamily(this, DeviceFamily.Phone);

        AppInjector.init(this);

        //Init Core Components
        getAppManager().setDebugMode(Constants.VALUES.DEBUG_MODE);
        getAppManager().setLogRetrofit(Constants.VALUES.LOG_RETROFIT);
        DataUpdateNotificationBuilder.initializeNotifications(this);
        getAppManager().updateSettings();


        //Init Firebase
        FirebaseApp.initializeApp(this);
    }
}
