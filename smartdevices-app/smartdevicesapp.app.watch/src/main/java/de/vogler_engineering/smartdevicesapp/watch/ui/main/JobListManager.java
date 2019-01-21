/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import de.vogler_engineering.smartdevicesapp.common.misc.BiConsumer;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableRecyclerAdapter;

public class JobListManager {

    private final MainActivity activity;
    private final WearableRecyclerView mRecyclerView;
    private final MainViewModel viewModel;
    private final ComponentFactory componentFactory;
    private final JobRepository jobRepository;

    private final MutableLiveData<Boolean> containsData = new MutableLiveData<>();
    private BiConsumer<String, Integer> mTabEntryCountChangeListener;

    public JobListManager(MainActivity activity,
                          WearableRecyclerView recyclerView,
                          MainViewModel viewModel,
                          ComponentFactory componentFactory, JobRepository jobRepository) {
        this.activity = activity;
        this.mRecyclerView = recyclerView;
        this.viewModel = viewModel;
        this.componentFactory = componentFactory;
        this.jobRepository = jobRepository;
    }

    public void initialize(){
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        mRecyclerView.setLayoutManager(
                new WearableLinearLayoutManager(activity));

        ConfigurableRecyclerAdapter<UiComponent> adapter =
                new ConfigurableRecyclerAdapter<>(activity, viewModel.getComponentDataProvider(),
                        componentFactory, activity);
        adapter.setItems(viewModel.getItems());
        mRecyclerView.setAdapter(adapter);

        updateContainsData(viewModel.getItems().size());

        mTabEntryCountChangeListener = (tabKey, count) -> {
            if(tabKey.equals("dashboard")){
                updateContainsData(count);
            }
        };
        jobRepository.setTabCountChangeListener(mTabEntryCountChangeListener);
    }

    public void registerContextReceivers() {
        jobRepository.setTabCountChangeListener(mTabEntryCountChangeListener);
    }

    public void unRegisterContextReceivers() {
        jobRepository.setTabCountChangeListener(null);
    }

    private void updateContainsData(int count) {
        if(count > 0){
            containsData.postValue(true);
        }else{
            containsData.postValue(false);
        }
    }

    public boolean getContainsData() {
        Boolean b = containsData.getValue();
        return b == null ? false : b;
    }

    public LiveData<Boolean> getContainsDataObservable() {
        return containsData;
    }
}
