/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.main;

import android.content.Intent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.concurrent.TimeUnit;

import de.vogler_engineering.smartdevicesapp.app.phone.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Class to manage the SwipeRefreshLayout on an Activity.
 */
public class SwipeRefreshManager {

    private static final String TAG = "SwipeRefreshManager";
    private final static int SWIPE_UPDATE_DURATION = 5; //Seconds

    private final AbstractActivity activity;
    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Disposable mResetTimer;

    public SwipeRefreshManager(AbstractActivity activity, AppManager appManager, SchedulersFacade schedulersFacade, SwipeRefreshLayout mSwipeRefreshLayout) {
        this.activity = activity;
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
    }

    public void init(){
        appManager.getIsUpdatingObservable().observe(activity, g -> {
            if(!appManager.getIsUpdating())
            {
                mSwipeRefreshLayout.setRefreshing(false);

                //Reset Timer if needed
                if(mResetTimer != null)
                    mResetTimer.dispose();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startSwipeRefresh();
            }
        });
        mSwipeRefreshLayout.setEnabled(true);
    }

    public void startSwipeRefresh() {
        Timber.tag(TAG).i("Swipe update data!");
        Intent intent = UpdateService.createStartUpdateServiceIntent(
                activity, Constants.ACTIONS.GET_ALL_NOTIFICATION);
        appManager.getAppContext().startService(intent);

        mResetTimer = Single.timer(SWIPE_UPDATE_DURATION, TimeUnit.SECONDS)
                .observeOn(schedulersFacade.ui())
                .subscribe(success -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showUnsuccessfullMessage();
                    Timber.tag(TAG).d("Swipe Update timeout");
                }, error  -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showUnsuccessfullMessage();
                    Timber.tag(TAG).d("Swipe Update timeout canceled!");
                });
    }

    private void showUnsuccessfullMessage() {
        Timber.tag(TAG).e("Swipe Update has encountert an error!");
    }
}
