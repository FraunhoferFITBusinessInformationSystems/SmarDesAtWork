/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceInfo;
import de.vogler_engineering.smartdevicesapp.model.entities.enums.DeviceFamily;
import de.vogler_engineering.smartdevicesapp.model.entities.enums.DeviceType;

public class DeviceInfoUtils {

    public final static String KEY_DENSITY = "Density";
    public final static String KEY_DIMENSIONS = "Dimensions";
    public final static String KEY_API_LEVEL = "ApiLevel";
    public final static String KEY_VERSION = "ReleaseVersion";
    public final static String KEY_BUILD_ID = "BuildId";

    public final static String KEY_MANUFACTURER = "Manufacturer";
    public final static String KEY_BRAND = "Brand";
    public final static String KEY_MODEL = "Model";

    private static DeviceFamily deviceFamily = DeviceFamily.Unknown;

    public static String getScreenDensity(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                return "LDPI";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "MDPI";
            case DisplayMetrics.DENSITY_HIGH:
                return "HDPI";
            case DisplayMetrics.DENSITY_XHIGH:
                return "XHDPI";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "XXHDPI";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "XXXHDPI";
            case DisplayMetrics.DENSITY_TV:
                return "TVDPI";
            default:
                return "Unknown";
        }
    }

    public static String getScreenDimension(Context context) {
        int screenLayoutSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenLayoutSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "Small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "Normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "Large";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "Xlarge";
            default:
                return "Unknown";
        }
    }

    public static String getDeviceName(){
        return String.format("%s %s", Build.MANUFACTURER, Build.MODEL);
    }

    public static String getApiLevel(){
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public static String getReleaseVersion(){
        return Build.VERSION.RELEASE;
    }

    public static Map<String, String> getInfos(Context context) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_DENSITY, getScreenDensity(context));
        map.put(KEY_DIMENSIONS, getScreenDimension(context));
        map.put(KEY_API_LEVEL, "" + Build.VERSION.SDK_INT);
        map.put(KEY_VERSION, Build.VERSION.RELEASE);
        map.put(KEY_BUILD_ID, Build.DISPLAY);

        map.put(KEY_MANUFACTURER, Build.MANUFACTURER);
        map.put(KEY_BRAND, Build.BRAND);
        map.put(KEY_MODEL, Build.MODEL);

        return map;
    }

    public static DeviceInfo buildDeviceInfo(Context context){
        DeviceInfo di = new DeviceInfo();
        di.setDeviceName(getDeviceName());
        di.setType(DeviceType.Android);
        di.setFamily(getDeviceFamily());
        di.setProperties(getInfos(context));
        return di;
    }

    public static String getAppVersion(Context context, String packageName){
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static void setDeviceFamily(Context context, DeviceFamily family) {
        if(family == DeviceFamily.Phone){
            if(context.getResources().getConfiguration().smallestScreenWidthDp < 600){
                deviceFamily = DeviceFamily.Phone;
            }else{
                deviceFamily = DeviceFamily.Tablet;
            }
        }
        deviceFamily = family;
    }

    public static DeviceFamily getDeviceFamily(){
        return deviceFamily;
    }
}