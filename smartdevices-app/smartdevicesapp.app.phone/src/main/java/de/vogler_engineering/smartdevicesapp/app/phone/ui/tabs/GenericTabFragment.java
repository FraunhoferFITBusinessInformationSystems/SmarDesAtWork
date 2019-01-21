/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.app.phone.ui.main.SwipeRefreshManager;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableRecyclerAdapter;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractFragment;
import timber.log.Timber;

public class GenericTabFragment  extends AbstractFragment<GenericTabViewModel> implements Injectable {

    private static final String TAG = "GenericTabFragment";
    private final static String TAB_KEY_NAME = "GenericTabFragmentKey";

    private FrameLayout mWrappingLayout;
    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLayoutManager;
    private ConfigurableRecyclerAdapter<UiComponent> mAdapter;
    private ComponentProviderViewModel mSubViewModel;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwipeRefreshManager mSwipeRefreshManager;

    private String tabKey = null;

    @Inject
    ComponentFactory componentFactory;

    @Inject
    AppManager appManager;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    public GenericTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        tabKey = args != null ? args.getString(TAB_KEY_NAME) : null;

        loadViewModel(GenericTabViewModel.class);
        viewModel.setTabKey(tabKey);
        Timber.tag(TAG).i("Viewmodel id: %s on GenericTabFragment /w tabKey: %s", viewModel.toString(), tabKey);

        // Build TabView
        mWrappingLayout = createLayout(inflater, container);

        // Init SwipeRefreshLayout
        FragmentActivity activity = getActivity();
        if(activity instanceof AbstractActivity) {
            AbstractActivity absActivity = (AbstractActivity) activity;
            mSwipeRefreshManager = new SwipeRefreshManager(absActivity, appManager, schedulersFacade, mSwipeRefreshLayout);
            mSwipeRefreshManager.init();
        }

        // Init Recyclerview
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Init Options Menu
        initOptionsMenu();

        initDefaultTab();

        return mWrappingLayout;
    }



    private void initOptionsMenu() {
        setHasOptionsMenu(true);

    }

    private void initDefaultTab(){
        mAdapter = new ConfigurableRecyclerAdapter<>(getContext(), viewModel.getComponentDataProvider(), this.componentFactory, this);
        mAdapter.setItems(viewModel.getItems());
        mRecyclerView.setAdapter(mAdapter);
    }

    private FrameLayout createLayout(LayoutInflater inflater, ViewGroup container){
        FrameLayout layout = new FrameLayout(container.getContext());
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //Swipe Refresh Layout
        mSwipeRefreshLayout = new SwipeRefreshLayout(container.getContext());
        mSwipeRefreshLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mRecyclerView = new RecyclerView(container.getContext());
        mRecyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRecyclerView.setVerticalScrollBarEnabled(true);
        layout.addView(mSwipeRefreshLayout);
        mSwipeRefreshLayout.addView(mRecyclerView);
        return layout;
    }

    public static GenericTabFragment create(String tabKey) {
        GenericTabFragment fragment = new GenericTabFragment();
        Bundle args = new Bundle();
        args.putString(TAB_KEY_NAME, tabKey);
        fragment.setArguments(args);
        return fragment;
    }
}
