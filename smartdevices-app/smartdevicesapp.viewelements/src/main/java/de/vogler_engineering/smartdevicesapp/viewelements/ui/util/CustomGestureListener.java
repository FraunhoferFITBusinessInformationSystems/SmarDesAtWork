/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.util;

import android.content.res.Resources;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import timber.log.Timber;

public abstract class CustomGestureListener extends GestureDetector.SimpleOnGestureListener{

    private static final String TAG = "CustomGestureListener";

    private final View mView;

    private final int DENSITY_INDEPENDENT_THRESHOLD = 50;
    private final int SWIPE_THRESHOLD_DISTANCE;

    public CustomGestureListener(View view) {
        mView = view;

        int threshold;
        try {
            Resources r = view.getResources();
            float density = r.getDisplayMetrics().density;
            threshold = (int) (DENSITY_INDEPENDENT_THRESHOLD * density);
        }catch(Exception e){
            Timber.tag(TAG).e(e, "Could not read density!");
            threshold = DENSITY_INDEPENDENT_THRESHOLD;
        }
        SWIPE_THRESHOLD_DISTANCE = threshold;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        mView.onTouchEvent(e);
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        onTouch();
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float abs = Math.abs(e1.getX() - e2.getX());
        if(Math.abs(velocityX) > Math.abs(velocityY) && abs > SWIPE_THRESHOLD_DISTANCE) {
            if (e1.getX() < e2.getX()) {
                return onSwipeRight();
            }
            if (e1.getX() > e2.getX()) {
                return onSwipeLeft();
            }
        }
        return onTouch();
    }

    public abstract boolean onSwipeRight();

    public abstract boolean onSwipeLeft();

    public abstract boolean onTouch();

}
