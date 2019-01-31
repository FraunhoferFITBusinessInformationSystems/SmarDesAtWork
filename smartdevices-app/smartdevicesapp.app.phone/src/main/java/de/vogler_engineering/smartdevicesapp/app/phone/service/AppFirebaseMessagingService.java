/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import de.vogler_engineering.smartdevicesapp.app.phone.ui.main.MainActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.DataUpdateNotificationBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions;
import de.vogler_engineering.smartdevicesapp.viewelements.service.AbstractFirebaseMessagingService;

import static de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions.getNotificationOptionsForPatternCode;


/**
 * Created by bf on 08.03.2018.
 */

public class AppFirebaseMessagingService extends AbstractFirebaseMessagingService {

    private static final String TAG = "FirebaseMsgSrv";

    public AppFirebaseMessagingService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    @Override
    protected void showNotification(String action, RemoteMessage msg, int patternCode) {
        DataUpdateNotificationBuilder builder = new DataUpdateNotificationBuilder(this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationOptions options = getNotificationOptionsForPatternCode(patternCode);
        Notification notification = builder.createNotification(pendingIntent, options);
        builder.sendNotification(notification, options);

        builder.dispose();
    }
}

