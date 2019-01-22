/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.util;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import timber.log.Timber;

public final class SoftKeyboardUtils {

    private static final String TAG = "SoftKeyboardUtils";
    
    private SoftKeyboardUtils() {
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm == null){
            Timber.tag(TAG).e("Could not get ImputMethodManager to close SoftKeyboard!");
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideSoftKeyboard(Fragment fragment){
        try {
            Context ctx = fragment.getContext();
            if (ctx == null) {
                Timber.tag(TAG).e("Could not get Context to close SoftKeyboard!");
                return;
            }
            InputMethodManager imm = (InputMethodManager) fragment.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm == null) {
                Timber.tag(TAG).e("Could not get ImputMethodManager to close SoftKeyboard!");
                return;
            }
            //Find the currently focused view, so we can grab the correct window token from it.
            //noinspection ConstantConditions
            View view = fragment.getView().getRootView();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(fragment.getContext());
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }catch (Exception e){
            Timber.tag(TAG).e(e, "Could not close SoftKeyboard.");
        }
    }

    public static void hideSoftKeyboard(Activity activity){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm == null){
            Timber.tag(TAG).e("Could not get ImputMethodManager to close SoftKeyboard!");
            return;
        }
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }
}
