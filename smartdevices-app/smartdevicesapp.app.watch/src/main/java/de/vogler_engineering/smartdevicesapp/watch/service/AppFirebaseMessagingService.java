/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.DataUpdateNotificationBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.service.AbstractFirebaseMessagingService;
import de.vogler_engineering.smartdevicesapp.watch.App;
import de.vogler_engineering.smartdevicesapp.watch.di.NotificationHandler;
import de.vogler_engineering.smartdevicesapp.watch.ui.main.MainActivity;
import timber.log.Timber;

import static de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions.getNotificationOptionsForPatternCode;


/**
 * Created by bf on 08.03.2018.
 */

public class AppFirebaseMessagingService extends AbstractFirebaseMessagingService {

    private static final String TAG = "FirebaseMsgSrv";

    public AppFirebaseMessagingService() {
        super();
    }

    @Inject
    NotificationHandler notificationHandler;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
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
        Timber.tag(TAG).d("Entering showNotification. Issue \"StartService\"-Intent. (CurrentContext: %x, App: %x)", this.hashCode(),
                this.getApplicationContext() != null ? this.getApplicationContext().hashCode() : 0);
//        Intent i = NotificationService.getPlayNotificationIntent(this, action, patternCode, msg);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startService(i);

        NotificationOptions options = getNotificationOptionsForPatternCode(patternCode);

        //Init the Notification service & Reset Timeout.
        notificationHandler.initService(this);

        try(DataUpdateNotificationBuilder builder = new DataUpdateNotificationBuilder(this)) {
            //Send the normal notification
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = builder.createNotification(pendingIntent, options);
            builder.sendNotification(notification, options);

            //On Watch use only normal notifications, no notification channel settings.
            //Channels didn't work with vibration and sound behaviour stored in there.
            //Start Vibrator and SoundPlayer
            Uri sound = builder.getSoundFromPreference(options);
            notificationHandler.startNotification(sound, options.getVibrationPattern());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
