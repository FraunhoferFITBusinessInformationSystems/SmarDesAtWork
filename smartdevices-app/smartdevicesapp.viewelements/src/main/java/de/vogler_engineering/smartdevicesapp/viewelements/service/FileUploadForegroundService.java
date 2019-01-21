/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.R;

/**
 * Created by vh on 12.03.2018.
 */

public class FileUploadForegroundService extends Service {

    private static final String LOG_TAG = "FileUploadService";

    private static final int NOTIFICATION_ID = 10;
    private static final String NOTIFICATION_CHANNEL_ID = "upload_service_01";

    private Notification notification;
    private float progress = 0f;

    public static boolean IS_SERVICE_RUNNING = false;

    @Override
    public void onCreate(){
        this.startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case Constants.ACTIONS.FILE_UPLOAD_SERVICE_START:
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
//                startForeground();
                Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();

                break;
            case Constants.ACTIONS.FILE_UPLOAD_SERVICE_STOP:
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
//                stopForeground(true);
//                stopSelf();

                break;
        }
        return START_STICKY;
    }

    private void startForeground(){
//        startForeground(NOTIFICATION_ID, createNotification(""));
    }

    private Notification createNotification(String text, PendingIntent pendingIntent){
        // The PendingIntent to launch our activity if the user selects
        // this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this,
//                0, new Intent(this, MainActivity.class),0);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(this.getBaseContext().getString(R.string.file_upload_service_title))
                .setContentText(text)
                .setOnlyAlertOnce(true) // so when data is updated don't make sound and alert in android 8.0+
                .setOngoing(true)
                .setProgress(100, (int)(progress*100), false)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_file_upload)
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification(){
        String text="Some text that will update the notification";

//        Notification notification = createNotification(text);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
