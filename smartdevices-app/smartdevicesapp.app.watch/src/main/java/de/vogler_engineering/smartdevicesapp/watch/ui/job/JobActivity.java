/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.job;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelper;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelpers;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityStateManager;
import de.vogler_engineering.smartdevicesapp.watch.helper.MultiFunctionButtonHelper;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import de.vogler_engineering.smartdevicesapp.watch.NavigationController;
import de.vogler_engineering.smartdevicesapp.watch.R;
import de.vogler_engineering.smartdevicesapp.watch.di.NotificationHandler;
import de.vogler_engineering.smartdevicesapp.watch.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.watch.ui.AbstractWearableActivity;
import timber.log.Timber;

public class JobActivity extends AbstractWearableActivity {

    private static final String TAG = "JobActivity";

    public static final String PARAM_JOB_ID = "JOB_ID";

    private ActivityStateManager mActivityStateManager;
    private JobViewModel viewModel;
    private LayoutHelper configurableHelper;

    @Inject
    NavigationController navigationController;

    @Inject
    NotificationHandler notificationHandler;

    @Inject
    AppManagerImpl appManager;

    @Inject
    BaseServiceManager serviceManager;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    UiMessageUtil messageUtil;

    @Inject
    TabRepository tabConfigRepository;

    @Inject
    ComponentFactory componentFactory;

    @Inject
    UiMessageUtil uiMessageUtil;

    @Inject
    LayoutHelpers layoutHelpers;

    @Inject
    DynamicValueRepository dynamicValueRepository;

    @BindView(R.id.activity_job_container)
    LinearLayout mContainer;

    @BindView(R.id.activity_job_container_layout)
    ScrollView mContainerLayout;

    @BindView(R.id.activity_job_loading_view)
    View mLoadingView;

    @BindView(R.id.activity_job_no_connection_view)
    View mOfflineView;

    @BindView(R.id.activity_job_no_data_view)
    View mNoDataView;

    @BindView(R.id.activity_job_error_view)
    View mErrorView;

    @BindView(R.id.view_error_details)
    TextView mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        ButterKnife.bind(this);

        dynamicValueRepository.disablePolling();

        //Init Application
        appManager.setActivityContext(this);
        if(!appManager.isInitialized()){
            storeFirebaseToken(FirebaseInstanceId.getInstance().getToken());
        }

        //Evaluate parameters
        Bundle b = getIntent().getExtras();
        UUID jobId = null;
        if(b != null) {
            String id = b.getString(PARAM_JOB_ID);
            try {
                jobId = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if(b == null || jobId == null){
            Timber.tag(TAG).e("Could not start JobActivity: No valid parameters supplied!");
            messageUtil.makeToast(R.string.job_toast_open_failure);
            super.finish();
        }

        //Load ViewModel
        viewModel = loadViewModel(JobViewModel.class);

        //Init View States
        mActivityStateManager = new ActivityStateManager(viewModel.getLoadingStateObservable(), mContainerLayout);
        mActivityStateManager.setContainer(ActivityLoadingState.Loading, mLoadingView);
        mActivityStateManager.setContainer(ActivityLoadingState.Offline, mOfflineView);
        mActivityStateManager.setContainer(ActivityLoadingState.Error, mErrorView);

        //Register ActivityFinished signal
        viewModel.getActivityFinishedObservable().observe(this, (v) -> this.finish());

        //Register LoadingFinished signal
        viewModel.getLoadingFinishedObservable().observe(this, (v) -> this.loadingFinished());
        viewModel.getErrorTextObservable().observe(this, t -> {
            if(mErrorText != null) mErrorText.setText(t);
        });

        notificationHandler.initActivity(this);

        viewModel.loadJobAsync(jobId);
    }

    private void loadingFinished() {
        try {
            buildView();
            viewModel.postLoadingState(ActivityLoadingState.Active);
        } catch (LayoutHelpers.UnknownLayoutHelperException e) {
            Timber.tag(TAG).e(e,"Could not parse JobLayout");
            messageUtil.makeToast(R.string.job_toast_open_failure);
        }
    }

    private void buildView() throws LayoutHelpers.UnknownLayoutHelperException {
        final UiLayout uiLayout = viewModel.getUiLayout();
        configurableHelper = layoutHelpers.createLayoutHelper(uiLayout);
        mContainer.removeAllViews();
        configurableHelper.createViews(this, getLayoutInflater(), mContainer, this);
        configurableHelper.bindViews(this, viewModel.getLayoutViewModel(), this);
        this.setTitle(uiLayout.getTitle());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Start UpdateService
        Intent intent = UpdateService.createStartUpdateServiceIntent(this,
                Constants.ACTIONS.GET_ALL_NOTIFICATION);
        appManager.getAppContext().startService(intent);

        //Register receivers
        registerContextReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterContextReceivers();
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
