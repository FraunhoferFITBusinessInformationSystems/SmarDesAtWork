/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import androidx.databinding.ObservableArrayList;

import java.util.Collections;
import java.util.List;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;

public class TabRepository extends AbstractRepository{

    private final ObservableArrayList<TabConfig> tabConfig = new ObservableArrayList<>();
    private final ConfigRepository configRepository;
    private final RestServiceProvider restServiceProvider;

    public static boolean GET_ONLY_ONE_TAB = false;
    public static String GET_ONLY_ONE_TAB_NAME = null;

    public TabRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, ConfigRepository configRepository) {
        super(appManager, schedulersFacade);
        this.configRepository = configRepository;
        this.restServiceProvider = restServiceProvider;

//        tabConfig.addAll(generateDefaultTabs());
        configRepository.getDeviceConfigObservable().observeForever((c) -> {
            if(c != null && c.getTabConfig() != null) {
                if(TabRepository.GET_ONLY_ONE_TAB){
                    configChangedOnlyOneTab(c.getTabConfig(), TabRepository.GET_ONLY_ONE_TAB_NAME);
                }else {
                    configChanged(c.getTabConfig());
                }
            }
        });
    }

    private void configChangedOnlyOneTab(List<TabConfig> tabConfig, String tabName) {
        for (TabConfig config : tabConfig) {
            if(config.getKey().equals(tabName)){
                List<TabConfig> newList = Collections.singletonList(config);
                configChanged(newList);
                return;
            }
        }

    }

    public void configChanged(List<TabConfig> newConfig) {
        //remove old, no more existing tabs (by key)
        for(int i = 0; i < tabConfig.size(); i++){
            int idx = getTabIndex(newConfig, tabConfig.get(i).getKey());
            if(idx == -1) {
                tabConfig.remove(i);
                i--;
            }
        }

        for (int i=0;i<newConfig.size();i++){
            if(i >= tabConfig.size()) {
                //add at the end
                tabConfig.add(newConfig.get(i));
            } else {
                int idx = getTabIndex(tabConfig, newConfig.get(i).getKey());
                if (idx == -1) {
                    tabConfig.add(i, newConfig.get(i));
                } else if (idx == i) {
                    //position is correct, compare tab contents
                    if(!tabConfig.get(i).deepEquals(newConfig.get(i))) {
                        tabConfig.set(i, newConfig.get(i));
                    }
                } else {
                    //different positions, remove old tab and add new
                    tabConfig.remove(idx);
                    tabConfig.add(i, newConfig.get(i));
                }
            }
        }
    }

    private int getTabIndex(List<TabConfig> list, String key){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getKey().equals(key)) return i;
        }
        return -1;
    }

    public ObservableArrayList<TabConfig> getTabConfig(){
        return tabConfig;
    }

    @Override
    public void updateData() {

    }

    @Override
    public void updateConfig() {

    }
}
