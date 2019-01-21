/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.main;

import androidx.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Bundle;
import androidx.wear.widget.WearableRecyclerView;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;
import android.view.KeyEvent;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;
import de.vogler_engineering.smartdevicesapp.model.repository.DeviceInfoRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityStateManager;
import de.vogler_engineering.smartdevicesapp.watch.NavigationController;
import de.vogler_engineering.smartdevicesapp.watch.R;
import de.vogler_engineering.smartdevicesapp.watch.di.NotificationHandler;
import de.vogler_engineering.smartdevicesapp.watch.helper.MultiFunctionButtonHelper;
import de.vogler_engineering.smartdevicesapp.watch.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.watch.ui.AbstractWearableActivity;


public class MainActivity extends AbstractWearableActivity {

    private static final String TAG = "MainActivity";

    public static boolean isRunning;

    public static final String JOB_DETAIL = "JOB_DETAIL";

    @Inject
    ComponentFactory componentFactory;

    @Inject
    NavigationController navigationController;

    @Inject
    RestServiceProvider restServiceProvider;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    DeviceInfoRepository deviceInfoRepository;

    @Inject
    JobRepository jobRepository;

    @Inject
    AppManagerImpl appManager;

    @Inject
    NotificationHandler notificationHandler;

    private final MutableLiveData<ActivityLoadingState> mActivityState = new MutableLiveData<>();

    private NavigationDrawerManager mNavigationDrawerManager;
    private JobListManager mJobListManager;
    private MainViewModel viewModel;

    @BindView(R.id.top_navigation_drawer)
    WearableNavigationDrawerView mWearableNavigationDrawerView;

    @BindView(R.id.recycler_list_view)
    WearableRecyclerView mRecyclerView;

    @BindView(R.id.loading_view)
    View mLoadingView;

    @BindView(R.id.no_connection_view)
    View mNoConnectionView;

    @BindView(R.id.no_data_view)
    View mNoDataView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setEchoEnabled(true);

        //Init App
        appManager.setActivityContext(this);
        appManager.setMainContext(this);
        navigationController.init(this);

        //Init Firebase
        storeFirebaseToken(FirebaseInstanceId.getInstance().getToken());

        //Top Navigation Drawer
        mNavigationDrawerManager = new NavigationDrawerManager(this, mWearableNavigationDrawerView);
        mNavigationDrawerManager.initializeDrawer();

        //Load ViewModel
        viewModel = loadViewModel(MainViewModel.class);

        //Job Recycler View Manger
        mJobListManager = new JobListManager(this, mRecyclerView, viewModel, componentFactory, jobRepository);
        mJobListManager.initialize();

        //Loading State Manager
        mActivityState.setValue(ActivityLoadingState.Loading);
        ActivityStateManager activityStateManager = new ActivityStateManager(mActivityState, mRecyclerView);
        activityStateManager.setContainer(ActivityLoadingState.Offline, mNoConnectionView);
        activityStateManager.setContainer(ActivityLoadingState.Empty, mNoDataView);
        activityStateManager.setContainer(ActivityLoadingState.Loading, mLoadingView);
        activityStateManager.updateVisibility();

        mJobListManager.getContainsDataObservable().observe(this, (b) -> {
            final boolean dataPresent = b == null ? false : b;
            updateLoadingState(appManager.getOnlineState(), dataPresent);
        });


//        imageViewStart.setImageResource(R.mipmap.ic_launcher_round);
        notificationHandler.initActivity(this);

        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appManager.setMainContext(this);

        //Start UpdateService
        Intent intent = UpdateService.createStartUpdateServiceIntent(this,
                Constants.ACTIONS.GET_ALL_NOTIFICATION);
        appManager.getAppContext().startService(intent);

        //Register receivers
        registerContextReceivers();
        mJobListManager.registerContextReceivers();

        //TODO show online state somewhere
        //appManager.getOnlineStateObservable().observe(this, x -> this.mDrawerManager.updateDrawer());
        appManager.getOnlineStateObservable().observe(this, (os) ->
                updateLoadingState(os, mJobListManager.getContainsData()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterContextReceivers();
        mJobListManager.unRegisterContextReceivers();
        appManager.getOnlineStateObservable().removeObservers(this);
    }

    @OnClick(R.id.view_loading_icon)
    void onImageclick() {
        appManager.getAppContext().startService(new Intent(appManager.getAppContext(), UpdateService.class));
        notificationHandler.stopSound();
        notificationHandler.stopVibrating();
    }

    private void updateLoadingState(OnlineState os, boolean dataPresent) {
        if (isInitialLoad()) {
            mActivityState.postValue(ActivityLoadingState.Loading);
        } else if (os == OnlineState.OFFLINE) {
            mActivityState.postValue(ActivityLoadingState.Offline);
        } else if (os == OnlineState.ONLINE || os == OnlineState.PENDING) {
            if (dataPresent) {
                mActivityState.postValue(ActivityLoadingState.Active);
            } else {
                mActivityState.postValue(ActivityLoadingState.Empty);
            }
        }
    }

    @Override
    public Class<?> getUpdateServiceClass() {
        return UpdateService.class;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(notificationHandler != null)
            notificationHandler.stopNotification();

        boolean b = MultiFunctionButtonHelper.getInstance().onKeyDown(keyCode, event);
        return b || super.onKeyDown(keyCode, event);
    }
}
