/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.util;

import android.annotation.SuppressLint;
import androidx.annotation.StringRes;
import android.widget.Toast;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import io.reactivex.Single;
import timber.log.Timber;

public class UiMessageUtil {

    private static final String TAG = "UiMessageUtil";

    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;

    @Inject
    public UiMessageUtil(AppManager appManager, SchedulersFacade schedulersFacade){
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
    }

    public void makeToast(String message){
        makeToast(message, Toast.LENGTH_LONG);
    }

    @SuppressLint("CheckResult")
    private void makeToast(String message, int length) {
        Single.fromCallable(() -> {
            Toast.makeText(appManager.getAppContext(), message, length).show();
            return true;
        })
                .subscribeOn(schedulersFacade.ui())
                .subscribe((d) -> {}, e -> Timber.tag(TAG).e(e, "Error while toasting toast!"));
    }

    public void makeToast(@StringRes int stringRes){
        makeToast(stringRes, Toast.LENGTH_LONG);
    }

    @SuppressLint("CheckResult")
    private void makeToast(@StringRes int stringRes, int length) {
        Single.fromCallable(() -> {
            Toast.makeText(appManager.getAppContext(), stringRes, length).show();
            return true;
        })
                .subscribeOn(schedulersFacade.ui())
                .subscribe((d) -> {}, e -> Timber.tag(TAG).e(e, "Error while toasting toast!"));
    }

    public void makeDebugToast(String message) {
        if (appManager.isDebugMode()){
            makeToast(message, Toast.LENGTH_LONG);
        }
    }
}
