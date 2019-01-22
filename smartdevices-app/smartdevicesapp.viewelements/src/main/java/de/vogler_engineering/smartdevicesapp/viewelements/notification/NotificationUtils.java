/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;

import timber.log.Timber;

public final class NotificationUtils {

    private static final String TAG = "NotificationUtils";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context,
                                                 NotificationOptions options) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Timber.tag(TAG).e("Could not create Notification Manager!");
            return;
        }

        NotificationChannel mChannel = notificationManager.getNotificationChannel(options.getChannelId());
        if (mChannel == null) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(
                    options.getChannelId(),
                    context.getResources().getString(options.getChannelName()),
                    importance);
            channel.setDescription(context.getResources().getString(options.getChannelDescription()));

            if (options.getLightColor() >= 0) {
                channel.setLightColor(options.getLightColor());
                channel.enableLights(true);
            }
            if (options.getVibrationPattern() != null) {
                channel.setVibrationPattern(options.getVibrationPattern());
                channel.enableVibration(true);
            }
            channel.setLockscreenVisibility(options.getVisibility());
            channel.setBypassDnd(options.isBypassDnd());

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getNotificationChannel(Context context, String channelId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Timber.tag(TAG).e("Could not create Notification Manager!");
            return null;
        }

        return notificationManager.getNotificationChannel(channelId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void updateChannelSound(Context context, NotificationOptions option, Uri sound) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Timber.tag(TAG).e("Could not create Notification Manager!");
            return;
        }
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(option.getChannelId());
        AudioAttributes att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        notificationChannel.setSound(sound, att);
        return;
    }
}
