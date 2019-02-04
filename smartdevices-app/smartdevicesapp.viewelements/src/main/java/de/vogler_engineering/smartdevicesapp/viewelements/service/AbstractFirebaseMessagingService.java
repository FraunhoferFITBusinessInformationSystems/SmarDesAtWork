/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.service;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.SmartDevicesApplication;
import timber.log.Timber;

public abstract class AbstractFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "AbstrFirebaseMsgSrv";

    public final static int DEFAULT_PATTERN_CODE = 0;

    public AbstractFirebaseMessagingService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Timber.tag(TAG).v("onMessageReceived()");
            Map<String, String> data = remoteMessage.getData();

            if (data == null || !data.containsKey("action")){
                Timber.tag(TAG).w("Firebase Message does not contain data field 'action'. Ignoring message!");
                return;
            }

            int pattern = DEFAULT_PATTERN_CODE;
            if(data.containsKey("pattern")){
                try {
                    pattern = Integer.parseInt(data.get("pattern"));
                }catch(NumberFormatException e){
                    Timber.tag(TAG).w("Could not read pattern from FCMBody. Value: %s", data.get("pattern"));
                }
            }

            String action = data.get("action");
            boolean notification = data.containsKey("notification")
                    && data.get("notification") != null
                    && data.get("notification").equalsIgnoreCase("true");

            Timber.tag(TAG).d("Received firebase message Action: %s, Pattern: %d, ID: %s, From: %s, Notification: %s",
                    action, pattern, remoteMessage.getMessageId(), remoteMessage.getFrom(), notification);

            if(notification){
                showNotification(action, remoteMessage, pattern);
            }

            //If app is running: send always the broadcast to the app.
            if(SmartDevicesApplication.isAppRunning()) {
                Timber.tag(TAG).d("App is running -> Relay Notification per BC");
                sendBroadcast(new Intent(action));
            }
        }catch (Exception e){
            Timber.tag(TAG).e(e.getMessage());
        }
    }

    @Override
    public void onNewToken(String s) {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.tag(TAG).i("Refreshed token: " + refreshedToken);

        Intent intent = new Intent(Constants.ACTIONS.GET_CONFIG_NOTIFICATION);
        intent.putExtra(Constants.EXTRAS.FIREBASE_TOKEN_KEY, refreshedToken);

        sendBroadcast(intent);

        super.onNewToken(s);
    }

    protected abstract void showNotification(String action, RemoteMessage msg, int patternCode);
}
