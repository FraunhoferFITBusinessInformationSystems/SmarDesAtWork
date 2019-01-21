/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.job;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
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
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.app.phone.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.main.DrawerManager;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelper;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelpers;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityStateManager;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import timber.log.Timber;

public class JobActivity extends AbstractActivity implements HasSupportFragmentInjector {

    private static final String TAG = "JobActivity";

    public static final String PARAM_JOB_ID = "JOB_ID";

    private DrawerManager mDrawerManager;
    private ActivityStateManager mActivityStateManager;
    private JobViewModel viewModel;
    private LayoutHelper configurableHelper;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    NavigationController navigationController;

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
    ConfigRepository configRepository;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ComponentFactory componentFactory;

    @Inject
    LayoutHelpers layoutHelpers;

    @Inject
    DynamicValueRepository dynamicValueRepository;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.activity_job_container)
    LinearLayout mContainer;

    @BindView(R.id.main_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.main_nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.activity_job_container_layout)
    ScrollView mContainerLayout;

    @BindView(R.id.activity_job_offline_view)
    View mOfflineView;

    @BindView(R.id.activity_job_loading_view)
    View mProgressView;

    @BindView(R.id.activity_job_no_data_view)
    View mNoDataView;

    @BindView(R.id.activity_job_error_view)
    View mErrorView;

    @BindView(R.id.view_error_details)
    TextView mErrorText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setEchoEnabled(true);
        setValueListening(false);

        //Init Application
        appManager.setActivityContext(this);
        if(!appManager.isInitialized()){
            storeFirebaseToken(FirebaseInstanceId.getInstance().getToken());
        }

        //Init Drawer Menu
        mDrawerManager = new DrawerManager(this, configRepository, resourceRepository, appManager, tabConfigRepository,
                navigationController,  mNavigationView);
        mDrawerManager.initDrawer(mDrawerLayout, mToolbar);
        mNavigationView.setNavigationItemSelectedListener(mDrawerManager);

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
            Timber.tag(TAG).e("Could not start JobActivity: No id parameter supplied!");
            messageUtil.makeToast(R.string.job_toast_open_failure);
            super.finish();
        }

        //Load ViewModel
        viewModel = loadViewModel(JobViewModel.class);

        //Init View States
        mActivityStateManager = new ActivityStateManager(viewModel.getLoadingStateObservable(), mContainerLayout);
        mActivityStateManager.setContainer(ActivityLoadingState.Loading, mProgressView);
        mActivityStateManager.setContainer(ActivityLoadingState.Offline, mOfflineView);
        mActivityStateManager.setContainer(ActivityLoadingState.Error, mErrorView);

        //Register ActivityFinished signal
        viewModel.getActivityFinishedObservable().observe(this, (v) -> this.finish());

        //Register LoadingFinished signal
        viewModel.getLoadingFinishedObservable().observe(this, (v) -> this.loadingFinished());
        viewModel.getErrorTextObservable().observe(this, t -> {
            if(mErrorText != null) mErrorText.setText(t);
        });

        try {
            viewModel.loadJobAsync(jobId);
        }catch (Exception e){
            viewModel.postError(e);
        }
    }

    private void loadingFinished() {
        try {
            buildView();
            viewModel.postLoadingState(ActivityLoadingState.Active);
        } catch (LayoutHelpers.UnknownLayoutHelperException e) {
            Timber.tag(TAG).e(e,"Could not parse JobLayout");
//            messageUtil.makeToast(R.string.job_toast_open_failure);
            viewModel.postLoadingState(ActivityLoadingState.Error);
            mErrorText.setText(e.toString());
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
//        Intent intent = UpdateService.createStartUpdateServiceIntent(this,
//                Constants.ACTIONS.GET_ALL_NOTIFICATION);
//        appManager.getAppContext().startService(intent);

        //Notify VM for updating its data
        if(viewModel != null && viewModel.getLayoutViewModel() != null) {
            viewModel.getLayoutViewModel().updateData();
        }

        //Register receivers
        registerContextReceivers();
        appManager.getOnlineStateObservable().observe(this, x -> this.mDrawerManager.updateDrawer());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterContextReceivers();
        appManager.getOnlineStateObservable().removeObservers(this);
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        configurableHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        viewModel.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public Class<?> getUpdateServiceClass() {
        return UpdateService.class;
    }
}
