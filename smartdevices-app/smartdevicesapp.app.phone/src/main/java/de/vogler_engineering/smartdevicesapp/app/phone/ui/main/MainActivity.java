/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;
import com.rahimlis.badgedtablayout.BadgedTabLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.app.phone.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityStateManager;


public class MainActivity extends AbstractActivity implements HasSupportFragmentInjector,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    public final static String PARAM_TAB_KEY = "TAB_KEY";

    private DrawerManager mDrawerManager;
    private OptionsMenuManager mOptionsMenuManager;
    private TabPagerManager mTabPagerManager;

    private final MutableLiveData<ActivityLoadingState> loadingState = new MutableLiveData<>();

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
    TabRepository tabConfigRepository;

    @Inject
    ConfigRepository configRepository;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    JobRepository jobRepository;

    @BindView(R.id.main_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.main_nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main_tab_layout)
    BadgedTabLayout mTabLayout;

    @BindView(R.id.main_offline_view)
    View mOfflineViewLayout;

    @BindView(R.id.main_loading_view)
    View mLoadingViewLayout;

    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setEchoEnabled(true);
        setValueListening(true);

        //Init App
        appManager.setActivityContext(this);
        appManager.setMainContext(this);
        navigationController.init(this);

        //Init Firebase
        storeFirebaseToken(FirebaseInstanceId.getInstance().getToken());

        //Init Drawer Menu
        mDrawerManager = new DrawerManager(this, configRepository, resourceRepository, appManager, tabConfigRepository, navigationController, mNavigationView);
        mDrawerManager.initDrawer(mDrawerLayout, mToolbar);
        mNavigationView.setNavigationItemSelectedListener(this);

        //Init Tabs
        mTabPagerManager = new TabPagerManager(this, appManager, tabConfigRepository, jobRepository, navigationController, mViewPager, mTabLayout, schedulersFacade);
        mTabPagerManager.initTabPager();

        //Init Options Menu
        mOptionsMenuManager = new OptionsMenuManager(this, appManager, mTabPagerManager, schedulersFacade);
        mOptionsMenuManager.initOptionsMenu();

        //Init View States
        loadingState.setValue(ActivityLoadingState.Loading);
        ActivityStateManager activityStateManager = new ActivityStateManager(
loadingState, mViewPager);
        activityStateManager.setContainer(ActivityLoadingState.Loading, mLoadingViewLayout);
        activityStateManager.setContainer(ActivityLoadingState.Offline, mOfflineViewLayout);
        //- Register ViewState Listeners
        mTabPagerManager.getContainsDataObservable().observe(this, (b) -> {
            final boolean dataPresent = b == null ? false : b;
            updateLoadingState(appManager.getOnlineState(), dataPresent);
        });



        if (!isStartedFromNotification() && savedInstanceState == null){
//            navigationController.navigateToTabs();
        } else {
            //navigationController.navigateToInit();
        }

        try {
            Bundle b = getIntent().getExtras();
            String tabKey = b.getString(PARAM_TAB_KEY, null);
            int i = mTabPagerManager.getTabIndex(tabKey);
            if (i < 0) {
                throw new Exception("Tab not found!");
            }
            mTabPagerManager.navigateToTabIndex(i);
        }catch (Exception e){
            mTabPagerManager.navigateToMainTab();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawerManager.updateDrawer();
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
        appManager.getOnlineStateObservable().observe(this, x -> this.mDrawerManager.updateDrawer());
        //- Register ViewState Listeners
        appManager.getOnlineStateObservable().observe(this, (os) -> updateLoadingState(os, mTabPagerManager.getContainsData()));
        mTabPagerManager.registerReceivers();

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterContextReceivers();
        appManager.getOnlineStateObservable().removeObservers(this);
        mTabPagerManager.unregisterReceivers();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mTabPagerManager.isCurrentTabMainTab()){
            super.onBackPressed();
        }else{
            mTabPagerManager.navigateToMainTab();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        mDrawerManager.onNavigationItemSelected(item);
        mDrawerLayout.closeDrawer(GravityCompat.START);
//        updateNavigationSelection();
        return true;
    }

    public void updateNavigationTabSelection(int position, TabConfig selectedTab) {
        int size = mNavigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            mNavigationView.getMenu().getItem(i).setChecked(i == position);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return mOptionsMenuManager.inflateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return mOptionsMenuManager.handleOptionsMenuItemSelected(item);
    }

    public ActivityLoadingState getLoadingState() {
        return loadingState.getValue();
    }

    public LiveData<ActivityLoadingState> getLoadingStateObservable() {
        return loadingState;
    }

    public void setLoadingState(ActivityLoadingState state){
        loadingState.postValue(state);
    }

    private void updateLoadingState(OnlineState os, boolean dataPresent){
        if(isInitialLoad()){
            loadingState.postValue(ActivityLoadingState.Loading);
        }else if(os == OnlineState.OFFLINE){
            loadingState.postValue(ActivityLoadingState.Offline);
        }
        else if(os == OnlineState.ONLINE || os == OnlineState.PENDING){
            if(dataPresent){
                loadingState.postValue(ActivityLoadingState.Active);
            }else{
                loadingState.postValue(ActivityLoadingState.Loading);
            }
        }
    }
}
