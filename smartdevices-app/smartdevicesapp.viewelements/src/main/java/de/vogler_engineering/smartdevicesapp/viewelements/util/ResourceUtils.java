/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.util;

import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.viewelements.R;

public class ResourceUtils {

    public final static int INVALID_RESOURCE = 0;

    private static Map<String, Integer> icons;

    public static int getIconResourceByKey(String key){
        if(icons == null) initIcons();
        if(icons.containsKey(key)){
            return icons.get(key);
        }
        return INVALID_RESOURCE;
    }

    private static void initIcons() {
        icons = new HashMap<>();
        icons.put("settings", R.drawable.ic_settings_black_24dp);
        icons.put("info", R.drawable.ic_info_black_24dp);
        icons.put("warn", R.drawable.ic_warning_black_24dp);
        icons.put("error", R.drawable.ic_cancel_black_24dp);
        icons.put("help", R.drawable.ic_help_black_24dp);
        icons.put("reply", R.drawable.ic_reply_black_24dp);
        icons.put("job", R.drawable.ic_assignment_black_24dp);
        icons.put("action", R.drawable.ic_play_circle_filled_black_24dp);
        icons.put("livefeed", R.drawable.ic_timeline_black_24dp);
        icons.put("message", R.drawable.ic_format_list_bulleted_black_24dp);
        icons.put("notification", R.drawable.ic_speaker_notes_black_24dp);
        icons.put("wrench", R.drawable.ic_build_black_24dp);
        icons.put("broken", R.drawable.ic_broken_image_black_24dp);
        icons.put("list", R.drawable.ic_format_list_bulleted_black_24dp);
        icons.put("play", R.drawable.ic_play_circle_filled_black_24dp);
        icons.put("sync", R.drawable.ic_sync_black_24dp);
        icons.put("photo", R.drawable.ic_photo_black_24dp);
        icons.put("camera", R.drawable.ic_camera_black_24dp);
        icons.put("add", R.drawable.ic_add_box_black_24dp);
        icons.put("remove", R.drawable.ic_remove_box_black_24dp);
        icons.put("check", R.drawable.ic_check_box_black_24dp);
        icons.put("barcode", R.drawable.ic_barcode_scan_24dp);
        icons.put("list_add", R.drawable.ic_playlist_add_black_24dp);
        icons.put("list_check", R.drawable.ic_playlist_add_check_black_24dp);
        icons.put("list_play", R.drawable.ic_playlist_play_black_24dp);

        icons.put("smartdes", R.drawable.ic_launcher_smartdes);
    }

    public enum AppDrawables {
        Settings,
        Info,
        Warn,
        Error,
        Help,
        Reply,
        Job,
        Action,
        Livefeed,
        Message,
        Notification,
        Wrench,
        Broken,
        List,
        Play,
        Sync,
        Photo,
        Camera,
        Add,
        Remove,
        Check,
        Barcode,
        ListAdd,
        ListCheck,
        ListPlay,
        Smartdes
    }

}
