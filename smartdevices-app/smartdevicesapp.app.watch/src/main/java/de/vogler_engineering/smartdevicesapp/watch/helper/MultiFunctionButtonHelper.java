/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.helper;

import android.app.Activity;
import android.os.Build;
import android.support.wearable.input.WearableButtons;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;

import de.vogler_engineering.smartdevicesapp.common.misc.BiFunction;

public class MultiFunctionButtonHelper {

    private boolean enabled = false;

    private int[] keys = null;

    private SparseBooleanArray availability = new SparseBooleanArray();
    private SparseArray<BiFunction<Integer, KeyEvent, Boolean>> listeners = new SparseArray<>();

    private static MultiFunctionButtonHelper mInstance = null;
    public static MultiFunctionButtonHelper getInstance() {
        if(mInstance == null) mInstance = new MultiFunctionButtonHelper();
        return mInstance;
    }

    public MultiFunctionButtonHelper() {
    }

    public void setup(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            keys = new int[]{ KeyEvent.KEYCODE_STEM_1, KeyEvent.KEYCODE_STEM_2, KeyEvent.KEYCODE_STEM_3 };
        }

        int count = WearableButtons.getButtonCount(activity);
        if (count == 0) {
            return;
        }

        for(int i = 0; i < count; i++) {
            WearableButtons.ButtonInfo buttonInfo =
                    WearableButtons.getButtonInfo(activity, keys[i]);
            if (buttonInfo != null) {
                availability.append(i, true);
            } else {
                availability.append(i, false);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(!enabled){
            return false;
        }
        if (event.getRepeatCount() == 0) {
            for (int key : keys) {
                BiFunction<Integer, KeyEvent, Boolean> listener = listeners.get(key);
                if (keyCode == key && listener != null) {
                    return listener.apply(keyCode, event);
                }
            }
        }
        return false;
    }
}
