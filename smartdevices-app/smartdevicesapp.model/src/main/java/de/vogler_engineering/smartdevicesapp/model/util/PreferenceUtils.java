/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.util;

import android.content.SharedPreferences;

import de.vogler_engineering.smartdevicesapp.model.entities.job.JobStatus;

public class PreferenceUtils {

    // GENERAL PREFERENCE KEYS
    public static final String PREF_KEY_INITIALIZED = "pref_key_preferences_initialized";

    // GET CONFIGS
    public static final String GETCONFIGNOTIFICATION = "GetConfig";
    public static final String GETDATANOTIFICATION = "GetData";
    public static final String GETALLNOTIFICATION = "GetAll";
    public static final String PUTCONFIGNOTIFICATION = "PutConfig";

    // PREFERENCE-Keys from Preference-Activity

    public final static String PREF_KEY_SERVER_ADDRESS = "pref_key_general_server_address";
    public final static String PREF_KEY_SERVER_PORT    = "pref_key_general_server_port";
    public final static String PREF_KEY_USERNAME       = "pref_key_general_username";
    public final static String PREF_KEY_DEVICENAME     = "pref_key_general_device_id";

    public final static String PREF_KEY_NOTIFICATION_DEFAULT  = "pref_key_notification_default";
    public final static String PREF_KEY_NOTIFICATION_PATTERN1 = "pref_key_notification_pattern1";
    public final static String PREF_KEY_NOTIFICATION_PATTERN2 = "pref_key_notification_pattern2";
    public final static String PREF_KEY_NOTIFICATION_PATTERN3 = "pref_key_notification_pattern3";

    //TODO introduce different default parameters for better watch installation
    public final static String PREF_DEF_SERVER_ADDRESS = "http://192.168.100.10";
    public final static String PREF_DEF_SERVER_PORT    = "7000";
    public final static String PREF_DEF_USERNAME       = "debug1";
    public final static String PREF_DEF_DEVICENAME     = "A";

    public static final String PREF_KEY_DRAWER_LAYOUT = "pref_key_design_drawerlayout";
    public static final String PREF_DEF_DRAWER_LAYOUT = "DrawerLayoutDefault";

    // LISTS
    public static final String LIST_ACTIONS = "ACTIONS";
    public static final String LIST_JOBS = "JOBS";
    public static final String LIST_LIVEDATA = "LIVEDATA";

    public static final String LIST_DIALOG_PREFIX = "TAB_PREF_";

    public static final String SORT_BY = "SORT_BY";
    public static final String SORT_ASC = "SORT_ASC";
    public static final String FILTER_ON_OFF = "FILTER_ON_OFF";
    public static final String FILTER_BY = "FILTER_BY";
    public static final String FILTER_STRING = "FILTER_STRING";
    public static final String FILTER_EXCLUDING = "FILTER_EXCLUDING";
    public static final String PAGEINATION_NUMBER = "PAGEINATION_NUMBER";
    public static final String PAGEINATION_SIZE = "PAGEINATION_SIZE";
    public static final String FILTER_STATE_ON_OFF = "FILTER_STATE_ON_OFF";
    public static final String FILTER_STATE_BY = "FILTER_STATE_BY";
    public static final String FILTER_STATE_EXCLUDING = "FILTER_STATE_EXCLUDING";


    public static final String MENU_TYPE_SORT = "SORT";
    public static final String MENU_TYPE_FILTER = "FILTER";
    public static final String MENU_TYPE_PAGE = "PAGE";


    public static final String VALUE_NAME_ASC = "ASC";
    public static final String VALUE_NAME_KEY = "KEY";
    public static final String VALUE_NAME_PNUMBER = "PAGENUMBER";
    public static final String VALUE_NAME_PSIZE = "PAGESIZE";

    public static final String VALUE_ASC = "asc";
    public static final String VALUE_DESC = "desc";

    public static final String POSTFIX_ENABLED = "enabled";
    public static final String POSTFIX_INVERTED = "inverted";


    public static String getMenuPrefVarName(String tabKey, String menuKey, String valueKey, String postfix){
        StringBuilder sb = new StringBuilder(LIST_DIALOG_PREFIX).append(tabKey)
                .append('_').append(menuKey)
                .append('_').append(valueKey);
        if(postfix != null){
            sb.append('_').append(postfix);
        }
        return sb.toString();
    }

    public static String getMenuPrefVarName(String tabKey, String menuKey, String valueKey){
        return getMenuPrefVarName(tabKey, menuKey, valueKey, null);
    }

    public static String getPrefVariableName(String listType, String optionName){
        return LIST_DIALOG_PREFIX + "_" + listType + "_" + optionName;
    }

    public static boolean isPreferencesInitialized(SharedPreferences pref){
        return pref.getBoolean(PREF_KEY_INITIALIZED, false);
    }

    public static void initializePreferences(SharedPreferences pref){
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(PREF_KEY_INITIALIZED, true);

        edit.putString(PREF_KEY_SERVER_ADDRESS, PREF_DEF_SERVER_ADDRESS);
        edit.putString(PREF_KEY_SERVER_PORT, PREF_DEF_SERVER_PORT);
        edit.putString(PREF_KEY_USERNAME, PREF_DEF_USERNAME);
        edit.putString(PREF_KEY_DEVICENAME, PREF_DEF_DEVICENAME);

        //Default status filter on tab 'dashboard'
        edit.putBoolean("TAB_PREF_dashboard_FILTER_status_inverted", true);
        edit.putString("TAB_PREF_dashboard_FILTER_status", JobStatus.Done.toString());
        edit.putBoolean("TAB_PREF_dashboard_FILTER_status_enabled", true);
        edit.putString("TAB_PREF_dashboard_SORT_ASC", VALUE_DESC);
        edit.putString("TAB_PREF_dashboard_SORT_KEY", "date");

        edit.apply();
    }

}
