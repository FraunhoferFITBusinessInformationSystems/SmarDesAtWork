/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.todostep;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.MembersInjector;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.app.phone.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelper;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelpers;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityStateManager;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist.TodoStepView;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist.TodoStepViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.util.CustomGestureListener;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import timber.log.Timber;

public class TodoStepActivity extends AbstractActivity implements HasSupportFragmentInjector {


    private static final String TAG = "TodoStepActivity";

    public static final String PARAM_LIST_ID = "LIST_ID";
    public static final String PARAM_INST_ID = "INSTANCE_ID";
    public static final String PARAM_STEP_NUM = "STEP_NUMBER";
    public static final String PARAM_CONTEXT_DOMAIN = "CONTEXT_DOMAIN";

    //    private DrawerManager mDrawerManager;
    private ActivityStateManager mActivityStateManager;
    private ActivityGestureListener mGestureListener;
    
    private TodoStepViewModel viewModel;
    private LayoutHelper configurableHelper;
    private TodoStepView view;

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
    ComponentFactory componentFactory;

    @Inject
    LayoutHelpers layoutHelpers;

    @Inject
    DynamicValueRepository dynamicValueRepository;

    @Inject
    MembersInjector<TodoStepView> todoStepViewInjector;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main_layout)
    View mDrawerLayout;

    @BindView(R.id.activity_todo_step_card)
    ConstraintLayout mContainerLayout;

    @BindView(R.id.activity_todo_step_offline_view)
    View mOfflineView;

    @BindView(R.id.activity_todo_step_loading_view)
    View mProgressView;

    @BindView(R.id.activity_todo_step_no_data_view)
    View mNoDataView;

    @BindView(R.id.activity_todo_step_error_view)
    View mErrorView;

    @BindView(R.id.activity_todo_step_swipe_detector)
    View mSwipeDetectorView;

    @BindView(R.id.view_error_details)
    TextView mErrorText;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_step);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setEchoEnabled(true);
        setValueListening(false);

        //Enable UP-Navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Init Application
        appManager.setActivityContext(this);
        if (!appManager.isInitialized()) {
            storeFirebaseToken(FirebaseInstanceId.getInstance().getToken());
        }

        //Gesture listener
        mGestureListener = new ActivityGestureListener(this, mSwipeDetectorView);

        //Evaluate parameters
        Bundle b = getIntent().getExtras();
        UUID instanceId = null;
        String listId = null;
        int stepNum = -1;
        String contextDomain = null;
        if (b != null) {
            String id = b.getString(PARAM_INST_ID);
            try {
                instanceId = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
            }
            listId = b.getString(PARAM_LIST_ID);
            stepNum = b.getInt(PARAM_STEP_NUM, -1);
            contextDomain = b.getString(PARAM_CONTEXT_DOMAIN);
        }
        if (b == null || instanceId == null || listId == null || contextDomain == null) {
            Timber.tag(TAG).e("Could not start TodoStepActivity: No parameters supplied!");
            messageUtil.makeToast(R.string.todo_step_toast_open_failure);
            super.finish();
            return;
        }

        //Load ViewModel
        viewModel = loadViewModel(TodoStepViewModel.class);

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
            if (mErrorText != null) mErrorText.setText(t);
        });

        view = new TodoStepView(this);
        todoStepViewInjector.injectMembers(view);
        view.createView(this, getLayoutInflater(), mContainerLayout);

        try {
            viewModel.loadData(listId, instanceId, stepNum, contextDomain);
        } catch (Exception e) {
            viewModel.postError(e);
        }
    }

    private boolean initialLoading = true;

    private void loadingFinished() {
        try {
            if (initialLoading) {
                view.bindView(this, viewModel, this);
                initialLoading = false;
            }
            viewModel.postLoadingState(ActivityLoadingState.Active);
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Could not open Todo-Step details");
            viewModel.postError("Could not open Todo-Step details");
        }
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
//        appManager.getOnlineStateObservable().observe(this, x -> this.mDrawerManager.updateDrawer());
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterContextReceivers();
        appManager.getOnlineStateObservable().removeObservers(this);
    }

    @Override
    public void finish() {
        hideSoftKeyboard();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public Class<?> getUpdateServiceClass() {
        return UpdateService.class;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureListener.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public class ActivityGestureListener extends CustomGestureListener {

        final GestureDetector mGestureDetector;

        public ActivityGestureListener(TodoStepActivity todoStepActivity, View view) {
            super(view);
            mGestureDetector = new GestureDetector(todoStepActivity, this);
        }

        public boolean onTouchEvent(MotionEvent event){
            return mGestureDetector.onTouchEvent(event);
        }

        @Override
        public boolean onSwipeLeft() {
            viewModel.nextCardClick(TodoStepActivity.this);
            return true;
        }

        @Override
        public boolean onSwipeRight() {
            viewModel.prevCardClick(TodoStepActivity.this);
            return true;
        }

        @Override
        public boolean onTouch() {
            return false;
        }
    }
}
