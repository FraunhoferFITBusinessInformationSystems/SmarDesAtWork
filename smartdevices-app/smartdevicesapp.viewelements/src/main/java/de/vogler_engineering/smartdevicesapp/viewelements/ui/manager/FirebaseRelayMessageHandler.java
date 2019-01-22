/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceInfo;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import timber.log.Timber;

public class FirebaseRelayMessageHandler {

    private static final String TAG = "FirebaseRelayMessageHandler";

    private final Activity activity;
    private final FirebaseMessageReceiver receiver;
    private AppManager appManager;
    private final ConfigRepository configRepository;

    private BroadcastReceiver updateMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Intent newIntent = new Intent(context, receiver.getUpdateServiceClass());
                newIntent.setAction(intent.getAction());
                appManager.getAppContext().startService(newIntent);
            } catch (Exception e) {
                Timber.tag(TAG).d(e.getMessage());
            }
        }
    };

    private BroadcastReceiver putConfigReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String newToken = intent.getStringExtra(Constants.EXTRAS.FIREBASE_TOKEN_KEY);

                DeviceInfo deviceInfo = configRepository.getDeviceInfo();
                deviceInfo.setFcmToken(newToken);

                configRepository.putDeviceInfo(deviceInfo);

                Toast.makeText(appManager.getAppContext(),
                        "Updated FcmToken",
                        Toast.LENGTH_SHORT)
                        .show();
            } catch (Exception e) {
                Timber.tag(TAG).d(e.getMessage());
            }
        }
    };

    public FirebaseRelayMessageHandler(Activity activity,
                                       FirebaseMessageReceiver receiver,
                                       AppManager appManager,
                                       ConfigRepository configRepository) {
        this.activity = activity;
        this.receiver = receiver;
        this.appManager = appManager;
        this.configRepository = configRepository;
    }

    public void registerContextReceivers() {
        activity.registerReceiver(updateMessageReceiver, new IntentFilter(Constants.ACTIONS.GET_CONFIG_NOTIFICATION));
        activity.registerReceiver(updateMessageReceiver, new IntentFilter(Constants.ACTIONS.GET_DATA_NOTIFICATION));
        activity.registerReceiver(updateMessageReceiver, new IntentFilter(Constants.ACTIONS.GET_ALL_NOTIFICATION));
        activity.registerReceiver(putConfigReceiver, new IntentFilter(Constants.ACTIONS.PUT_DATA_NOTIFICATION));
    }

    public void unregisterContextReceivers() {
        activity.unregisterReceiver(updateMessageReceiver);
        activity.unregisterReceiver(putConfigReceiver);
    }

    public void resolveBundleAction(String action) {
        if (Constants.ACTIONS.GET_CONFIG_NOTIFICATION.equals(action) ||
                Constants.ACTIONS.GET_DATA_NOTIFICATION.equals(action) ||
                Constants.ACTIONS.GET_ALL_NOTIFICATION.equals(action) ||
                Constants.ACTIONS.PUT_DATA_NOTIFICATION.equals(action)) {

            receiver.setStartedFromNotification(true);
        }
    }

    public interface FirebaseMessageReceiver {
        Class<?> getUpdateServiceClass();

        void setStartedFromNotification(boolean b);
    }
}
