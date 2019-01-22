/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.reactivex.disposables.Disposable;

public class DataUpdateNotificationBuilder implements Disposable, AutoCloseable {

    private Context mContext;

    private boolean disposed = false;

    public DataUpdateNotificationBuilder(Context context) {
        mContext = context;
    }

    public Notification createNotification(PendingIntent pendingIntent, NotificationOptions options) {
        String text = mContext.getResources().getString(options.getText());
        String title = mContext.getResources().getString(options.getTitle());


        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            createNotificationChannels(mContext);

            Notification.Builder builder = new Notification.Builder(mContext, options.getChannelId())
                    .setSmallIcon(options.getIcon())
                    .setContentTitle(text)
                    .setContentText(title)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setTicker(text);
            notification = builder.build();
        } else {
            Uri soundUri = getSoundFromPreference(options);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(mContext, options.getChannelId())
                            .setSmallIcon(options.getIcon())
                            .setContentTitle(text)
                            .setContentText(title)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setTicker(text)
                            .setVibrate(options.getVibrationPattern())
                            .setSound(soundUri)
                            .setLights(options.getLightColor(), 500, 500);
            builder.setPriority(Notification.PRIORITY_HIGH);
            notification = builder.build();
        }

        return notification;
    }

    public Uri getSoundFromPreference(NotificationOptions options){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String value = sharedPreferences.getString(options.getSoundPreferenceKey(), null);
        if(value != null) {
            return Uri.parse(value);
        }
        return Settings.System.DEFAULT_NOTIFICATION_URI;
    }

    public void sendNotification(Notification notification, NotificationOptions options) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(options.getId(), notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannels(Context mContext) {
        NotificationUtils.createNotificationChannel(mContext, NotificationOptions.Default);
        NotificationUtils.createNotificationChannel(mContext, NotificationOptions.Pattern1);
        NotificationUtils.createNotificationChannel(mContext, NotificationOptions.Pattern2);
        NotificationUtils.createNotificationChannel(mContext, NotificationOptions.Pattern3);
    }

    public static void initializeNotifications(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(context);
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            //Dispose
            mContext = null;
            disposed = true;
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void close() throws Exception {
        dispose();
    }
}
