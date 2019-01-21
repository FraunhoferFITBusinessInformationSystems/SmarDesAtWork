/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

public class DisplayUtils {

    private DisplayUtils(){
    }

    public static DisplayMetrics getMetrics(Context context){
        return context.getResources().getDisplayMetrics();
    }

    /**
     * Converts DP (Density-independent Pixels) to Total Pixels using the current Display-Metrics
     * @param metrics The {@link DisplayMetrics}-Object
     * @param dp The number of Density-independent Pixels (dp).
     * @return The total number of physical Pixels on the Screen.
     */
    public static int getFromDp(DisplayMetrics metrics, float dp){

        return (int) (dp * metrics.density + 0.5f);
    }

    /**
     * Converts DP (Density-independent Pixels) to Total Pixels using the current Display-Metrics
     * @param metrics The {@link DisplayMetrics}-Object
     * @param dp The number of Density-independent Pixels (dp).
     * @return The total number of physical Pixels on the Screen.
     */
    public static int getFromDp(DisplayMetrics metrics, int dp){
        return (int) (dp * metrics.density + 0.5f);
    }

    public static void setEnableStateRecursive(boolean enabled, View view) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setEnableStateRecursive(enabled, child);
            }
        }
    }
}
