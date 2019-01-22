/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.notification;

import android.app.Notification;
import android.graphics.Color;

import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import lombok.Data;

@Data
public final class NotificationOptions {

    private final long[] vibrationPattern;
    private final String channelId;
    private final int id;
    private final int lightColor;
    private final int visibility;
    private final int channelName;
    private final int channelDescription;
    private final int title;
    private final int text;
    private final int icon;
    private final boolean bypassDnd;
    private final String soundPreferenceKey;

    public final static NotificationOptions Default = new NotificationOptions(
            new long[]{0, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500},
            "smartdevicesapp.noti.default",
            100,
            Color.BLUE,
            Notification.VISIBILITY_PUBLIC,
            R.string.notification_default_channel_name,
            R.string.notification_default_channel_description,
            R.string.notification_default_title,
            R.string.notification_default_text,
            R.drawable.ic_assignment_sd_24dp,
            true,
            PreferenceUtils.PREF_KEY_NOTIFICATION_DEFAULT
    );
    public final static NotificationOptions Pattern1 = new NotificationOptions(
            new long[]{0, 500, 1500, 500, 1500, 500, 1500, 500},
            "smartdevicesapp.noti.pattern1",
            101,
            Color.BLUE,
            Notification.VISIBILITY_PUBLIC,
            R.string.notification_pattern1_channel_name,
            R.string.notification_pattern1_channel_description,
            R.string.notification_pattern1_title,
            R.string.notification_pattern1_text,
            R.drawable.ic_assignment_sd_24dp,
            true,
            PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN1
    );
    public final static NotificationOptions Pattern2 = new NotificationOptions(
            new long[]{0, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000},
            "smartdevicesapp.noti.pattern2",
            102,
            Color.YELLOW,
            Notification.VISIBILITY_PUBLIC,
            R.string.notification_pattern2_channel_name,
            R.string.notification_pattern2_channel_description,
            R.string.notification_pattern2_title,
            R.string.notification_pattern2_text,
            R.drawable.ic_warning_yellow_24dp,
            true,
            PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN2
    );
    public final static NotificationOptions Pattern3 = new NotificationOptions(
            new long[]{0, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500},
            "smartdevicesapp.noti.pattern3",
            103,
            Color.RED,
            Notification.VISIBILITY_PUBLIC,
            R.string.notification_pattern3_channel_name,
            R.string.notification_pattern3_channel_description,
            R.string.notification_pattern3_title,
            R.string.notification_pattern3_text,
            R.drawable.ic_report_red_24dp,
            true,
            PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN3
    );

    public static NotificationOptions getNotificationOptionsForPatternCode(int patternCode){
        switch (patternCode) {
            case 1: return NotificationOptions.Pattern1;
            case 2: return NotificationOptions.Pattern2;
            case 3: return NotificationOptions.Pattern3;
        }
        return NotificationOptions.Default;
    }

    public static NotificationOptions getNotificationOptionsForPreferenceKey(String preferenceKey){
        switch (preferenceKey) {
            case PreferenceUtils.PREF_KEY_NOTIFICATION_DEFAULT: return NotificationOptions.Default;
            case PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN1: return NotificationOptions.Pattern1;
            case PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN2: return NotificationOptions.Pattern2;
            case PreferenceUtils.PREF_KEY_NOTIFICATION_PATTERN3: return NotificationOptions.Pattern3;
        }
        return null;
    }
}
