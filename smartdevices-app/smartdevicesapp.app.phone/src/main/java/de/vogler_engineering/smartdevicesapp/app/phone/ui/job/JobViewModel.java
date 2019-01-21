/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.job;

import android.content.Context;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.app.phone.ui.main.MainActivity;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.AbstractDetailViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelpers;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;

public class JobViewModel extends AbstractDetailViewModel {

    private static final String TAG = "JobViewModel";

    private final AppManager appManager;

    @Inject
    public JobViewModel(AppManager appManager, SchedulersFacade schedulersFacade,
                        JobRepository jobRepository, LayoutHelpers layoutHelpers) {
        super(schedulersFacade, jobRepository, layoutHelpers);
        this.appManager = appManager;
        mLoadingState.setValue(ActivityLoadingState.Loading);
    }

    public UiLayout getUiLayout() {
        return getJobEntry().getUi();
    }

    @Override
    public void finishActivity() {
        hideSoftKeyboard();
        super.finishActivity();
    }

    private void hideSoftKeyboard() {
        Context ctx = appManager.getMainContext();
        if (ctx != null && ctx instanceof MainActivity) {
            MainActivity ma = (MainActivity) ctx;
            ma.hideSoftKeyboard();
        }
    }
}
