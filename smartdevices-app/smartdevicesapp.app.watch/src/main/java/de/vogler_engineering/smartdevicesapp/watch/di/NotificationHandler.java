/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.di;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.RequiresApi;

import javax.inject.Singleton;

import de.vogler_engineering.smartdevicesapp.viewelements.SmartDevicesApplication;
import timber.log.Timber;

/**
 * Created by bf on 13.03.2018.
 */


public class NotificationHandler {
    static private String TAG = "NotificationHandler";

    private Context context;
    private boolean isActivity = false;
    private MediaPlayer mMediaPlayer;
    private Vibrator v;

    private final static int NOTIFICATION_DURATION = 300000;

    private final Handler mTimeoutHandler = new Handler();
    private final Runnable stopNofificationRunnable = () -> {
        stopSound();
        stopVibrating();
    };

    @Singleton
    public NotificationHandler() {
    }


    public void startNotification(){
        long[] pattern = {0, 500, 500, 0, 500, 500};
        startNotification(pattern);
    }

    public void startNotification(long[] pattern){
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (sound == null) {
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (sound == null) {
                sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        startNotification(sound, pattern);
    }

    public void startNotification(Uri sound, long[] pattern){
        if (sound == null || pattern == null) {
            Timber.tag(TAG).e("No sound or pattern found for notification!");
            return;
        }

        startSound(sound);
        startVibrating(pattern);
        sheduleNotificationTimeout();
    }

    private void startSound(Uri sound) {
        if (!wearHasSpeaker(context)) {
            return;
        }

        stopSound();

        mMediaPlayer = MediaPlayer.create(context, sound);
        if (mMediaPlayer == null) {
            Timber.tag(TAG).e("Player is not set!");
            return;
        }

        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
    }


    private void startVibrating(long[] pattern) {
        Context ctx = tryGetAppContext();
        if(ctx == null) ctx = context;
        v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if(v == null){
            Timber.tag(TAG).e("Vibrator is null! Could not get Vibrator System Service!");
            return;
        }
        if(!v.hasVibrator()){
            Timber.tag(TAG).e("No Vibrator found.");
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect waveform = VibrationEffect.createWaveform(pattern, 0);
            v.vibrate(waveform);
        } else {
            v.vibrate(pattern, 0);
        }
    }

    private Context tryGetAppContext(){
        if(SmartDevicesApplication.getActualContext() != null){
            Timber.tag(TAG).d("Resolve Context to %s (SmartDevicesApplication.ActualContext)", SmartDevicesApplication.getActualContext().hashCode());
            return SmartDevicesApplication.getActualContext().getApplicationContext();
        }
        Timber.tag(TAG).d("Could'n resolve Active context!");
        return null;
    }

    public void stopSound() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void stopVibrating() {
        if (v != null) {
            v.cancel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean wearHasSpeaker(final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        // The results from AudioManager.getDevices can't be trusted unless the device advertises FEATURE_AUDIO_OUTPUT.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return false;
        }

        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager == null) return false;
        final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                return true;
            }
        }
        return false;
    }

    private void resetNotificationTimeout() {
        //Remove old callbacks
        mTimeoutHandler.removeCallbacks(stopNofificationRunnable);
        //Add new Callback with timeout
        sheduleNotificationTimeout();
    }

    public void stopNotification() {
        mTimeoutHandler.removeCallbacks(stopNofificationRunnable);
        stopSound();
        stopVibrating();
    }

    private void sheduleNotificationTimeout() {
        mTimeoutHandler.postDelayed(stopNofificationRunnable, NOTIFICATION_DURATION);
    }

    private void init(Context ctx) {
        if (ctx.getApplicationContext() != context) {
            context = ctx.getApplicationContext();
        } else {
            context = ctx;
        }
    }

    public void initService(Service service) {
        init(service);
        isActivity = false;
        resetNotificationTimeout();
    }

    public void initActivity(Activity act) {
        init(act);
        isActivity = true;
        stopNotification();
    }


//    public void showAppNotification(String title, String message) {
//        createNotificationChannels();
//
//        Intent intent = new Intent(context, MainActivity.class);
//
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentTitle("SmartDevices")
//                .setContentText("Neue Daten liegen vor.")
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setContentIntent(pendingIntent)
//                .setVibrate(new long[]{0, 500, 500, 500})
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//        Notification notification = mBuilder.build();
//
//        notificationManager.notify(42, notification);
//    }
//
//    // INFINITE VIBRATION
//    private void vibrationUsingPattern() {
//        long[] pattern = {0, 500, 500, 0, 500, 500};
//        v.vibrate(pattern, 0);
//    }
//
//    //ONE SHOT
//    private void createOneShotVibrationUsingVibrationPattern() {
//        v.vibrate(5000);
//    }
//
///*    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void vibrationUsingVibrationEffect() {
//        long[] mVibratePattern = new long[]{1000, 500, 500, 500};
//        // -1 : Play exactly once
//        VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, 0);
//        v.vibrate(effect);
//    }*/
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void createOneShotVibrationUsingVibrationEffect() {
//        // 1000 : Vibrate for 1 sec
//        // VibrationEffect.DEFAULT_AMPLITUDE - would perform vibration at full strength
//        VibrationEffect effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);
//        v.vibrate(effect);
//    }
//
//    private void createNotificationChannels() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
//            channel.setDescription(CHANNEL_ID);
//            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{0, 500, 500, 500});
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannels(channel);
//        }
//    }
}
