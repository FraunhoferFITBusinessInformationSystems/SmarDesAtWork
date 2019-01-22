/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.preferences;

import android.app.NotificationChannel;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.vogler_engineering.smartdevicesapp.model.entities.enums.DeviceFamily;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationOptions;
import de.vogler_engineering.smartdevicesapp.viewelements.notification.NotificationUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DeviceInfoUtils;
import timber.log.Timber;

public abstract class UserPreferenceFragmentBase extends PreferenceFragmentCompat {

    private static final String TAG = "UserPreferenceFragmentBase";

    private static final int REQUEST_CODE_DEFAULT_RINGTONE = 100;
    private static final int REQUEST_CODE_PATTERN1_RINGTONE = 101;
    private static final int REQUEST_CODE_PATTERN2_RINGTONE = 102;
    private static final int REQUEST_CODE_PATTERN3_RINGTONE = 103;

    protected static String getKeyFromRequestCode(int code){
        switch (code){
            case REQUEST_CODE_DEFAULT_RINGTONE:  return PreferenceUtils.PREF_KEY_NOTIFICATION_DEFAULT;
            case REQUEST_CODE_PATTERN1_RINGTONE: return PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN1;
            case REQUEST_CODE_PATTERN2_RINGTONE: return PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN2;
            case REQUEST_CODE_PATTERN3_RINGTONE: return PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN3;
        }
        return null;
    }

    protected static int getRequestCodeFromKey(String key){
        switch (key){
            case PreferenceUtils.PREF_KEY_NOTIFICATION_DEFAULT: return REQUEST_CODE_DEFAULT_RINGTONE ;
            case PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN1: return REQUEST_CODE_PATTERN1_RINGTONE;
            case PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN2: return REQUEST_CODE_PATTERN2_RINGTONE;
            case PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN3: return REQUEST_CODE_PATTERN3_RINGTONE;
        }
        return -1;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        int requestCode = getRequestCodeFromKey(key);
        if (requestCode != -1) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION | RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = getSound(key);
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, requestCode);
            return true;
        } else if(preference.getKey().equals("pref_key_notification_settings_link")) {
            return this.openSystemNotificationOptions(preference);
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    protected boolean openSystemNotificationOptions(Preference preference) {
        Context context = getContext();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && context != null) {
            try {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                startActivity(intent);
            }catch(ActivityNotFoundException e) {
                Timber.tag(TAG).i(e);
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode >= REQUEST_CODE_DEFAULT_RINGTONE &&
                requestCode <= REQUEST_CODE_PATTERN3_RINGTONE &&
                data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null) {
                setSound(getKeyFromRequestCode(requestCode), ringtone);
            } else {

                setPreferenceValue(getKeyFromRequestCode(requestCode), "");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getSound(String key){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getContext() != null &&
                DeviceInfoUtils.getDeviceFamily() != DeviceFamily.Watch) {
            NotificationOptions options = NotificationOptions.
                    getNotificationOptionsForPreferenceKey(key);
            NotificationChannel channel = null;
            Uri sound = null;
            if (options != null) {
                channel = NotificationUtils.
                        getNotificationChannel(getContext(), options.getChannelId());
                if (channel != null) {
                    sound = channel.getSound();
                }
            }
            return sound != null ? sound.toString() : "";
        }else{
            return getPreferenceValue(key);
        }
    }

    private void setSound(String key, Uri ringtone) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getContext() != null &&
                DeviceInfoUtils.getDeviceFamily() != DeviceFamily.Watch) {
            NotificationOptions options = NotificationOptions.
                    getNotificationOptionsForPreferenceKey(key);

            NotificationUtils.updateChannelSound(getContext(), options, ringtone);
        }
        //Store it always in the preferences
        // ringtone=null: "Silent" was selected
        setPreferenceValue(key, ringtone != null ? ringtone.toString() : "");
    }

    protected String getPreferenceValue(String key) {
        return PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getString(key, null);
    }

    protected void setPreferenceValue(String key, String url) {
        SharedPreferences.Editor edit = PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .edit();
        edit.putString(key, url);
        edit.commit();
    }
}
