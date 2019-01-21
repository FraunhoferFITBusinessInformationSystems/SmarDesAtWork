/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;

public abstract class LayoutView {

    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected WeakReference<LifecycleOwner> lifecycleOwner;

    public LayoutView() {
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = new WeakReference<>(lifecycleOwner);
    }

    public abstract View createView(Context context, LayoutInflater inflater, ViewGroup parentView);

    public abstract void bindView(Context context, LayoutViewModelReference viewModel, LifecycleOwner lifecycleOwner);

    public void dispose(){
        compositeDisposable.dispose();
    }

    public abstract View getView();

    public LinearLayout.LayoutParams getDefaultLayoutParams(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public int getFromDp(DisplayMetrics metrics, float dp){

        return (int) (dp * metrics.density + 0.5f);
    }

    public int getFromDp(DisplayMetrics metrics, int dp){
        return (int) (dp * metrics.density + 0.5f);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
    }
}
