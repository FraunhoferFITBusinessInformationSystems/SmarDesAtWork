package de.vogler_engineering.smartdevicesapp.watch.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.DataUpdateNotificationBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions;
import de.vogler_engineering.smartdevicesapp.watch.di.NotificationHandler;
import de.vogler_engineering.smartdevicesapp.watch.ui.main.MainActivity;
import timber.log.Timber;

import static de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions.getNotificationOptionsForPatternCode;

/**
 * An {@link IntentService} to manage Firebase Notifications. It gets called from the
 * {@link com.google.firebase.messaging.FirebaseMessagingService} and handles the Android-
 * Notification and supervises the Music Player and Vibrator.
 */
public class NotificationService extends IntentService {
    private static final String ACTION_PLAY_NOTIFICATION = "de.vogler_engineering.smartdevicesapp.watch.service.action.START_PLAY_NOTIFICATION";

    private static final String EXTRA_ACTION = "de.vogler_engineering.smartdevicesapp.watch.service.extra.ACTION";
    private static final String EXTRA_PATTERN_CODE = "de.vogler_engineering.smartdevicesapp.watch.service.extra.PATTERN_CODE";
    private static final String EXTRA_REMOTE_MESSAGE = "de.vogler_engineering.smartdevicesapp.watch.service.extra.REMOTE_MESSAGE";

    private static final String TAG = "NotificationService";

    @Inject
    NotificationHandler notificationHandler;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        Timber.tag(TAG).d("Service created!");
    }

    /**
     * Starts this service to perform action ACTION_PLAY_NOTIFICATION with the given parameters.
     * If the service is already performing a task this action will be queued.
     */
    public static Intent getPlayNotificationIntent(Context context, String action, int patternCode, RemoteMessage remoteMessage) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_PLAY_NOTIFICATION);
        intent.putExtra(EXTRA_ACTION, action);
        intent.putExtra(EXTRA_PATTERN_CODE, patternCode);
        intent.putExtra(EXTRA_REMOTE_MESSAGE, remoteMessage);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.tag(TAG).d("NotificationService started. (CurrentContext: %x, App: %x)", this.hashCode(),
                this.getApplicationContext() != null ? this.getApplicationContext().hashCode() : 0);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY_NOTIFICATION.equals(action)) {
                final String notificationAction = intent.getStringExtra(EXTRA_ACTION);
                final int patternCode = intent.getIntExtra(EXTRA_PATTERN_CODE, 0);
                final RemoteMessage remoteMessage = intent.getParcelableExtra(EXTRA_REMOTE_MESSAGE);
                handleActionPlayNotification(notificationAction, patternCode, remoteMessage);
            }
        }
    }

    private void handleActionPlayNotification(String action, int patternCode, RemoteMessage remoteMessage) {
        notificationHandler.initService(this);
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

            //Get current sound and vibration from notification channel, if api > oreo
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel channel = NotificationUtils.getNotificationChannel(this,
//                        options.getChannelId());
//                if (channel != null) {
//                    long[] pattern = channel.getVibrationPattern();
//                    Uri sound = channel.getSound();
//                    notificationHandler.startNotification(sound, pattern);
//                    return;
//                }
//            } else {
            //On Watch use only normal notifications, no notification channel settings.
            //Channels didn't work with vibration and sound behaviour stored in there.
            //Start Vibrator and SoundPlayer
            Uri sound = builder.getSoundFromPreference(options);
            notificationHandler.startNotification(sound, options.getVibrationPattern());
            //            }
//            notificationHandler.startNotification(options.getVibrationPattern());
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
        }
    }
}
