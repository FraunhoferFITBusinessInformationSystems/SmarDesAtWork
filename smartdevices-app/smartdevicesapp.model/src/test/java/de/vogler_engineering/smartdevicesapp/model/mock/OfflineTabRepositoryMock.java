/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import androidx.databinding.ObservableArrayList;

import java.util.ArrayList;
import java.util.List;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabFilterEntry;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabSortEntry;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;

public class OfflineTabRepositoryMock extends TabRepository {

    private final ObservableArrayList<TabConfig> tabConfigs = new ObservableArrayList<>();

    public OfflineTabRepositoryMock(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, ConfigRepository configRepository) {
        super(appManager, schedulersFacade, restServiceProvider, configRepository);

        tabConfigs.addAll(generateDemoTabs());
    }

    @Override
    public ObservableArrayList<TabConfig> getTabConfig() {
        return tabConfigs;
    }

    public static List<TabConfig> generateDemoTabs() {
        List<TabConfig> list = new ArrayList<>();
        TabConfig t;

        TabFilterEntry statusFilter = new TabFilterEntry();
        statusFilter.setKey("status");
        statusFilter.setInvertable(true);
        statusFilter.setName("Statusfilter");
        statusFilter.setFilterType("state");

        TabFilterEntry textFilter = new TabFilterEntry();
        textFilter.setKey("text");
        textFilter.setInvertable(true);
        textFilter.setName("Textfiler");
        textFilter.setFilterType("text");


        TabSortEntry stateSort = new TabSortEntry();
        stateSort.setKey("status");
        stateSort.setName("Status");

        TabSortEntry dateSort = new TabSortEntry();
        dateSort.setKey("createdAt");
        dateSort.setName("Datum");

        TabSortEntry typeSort = new TabSortEntry();
        typeSort.setKey("name");
        typeSort.setName("Typ");


        t = new TabConfig();
        t.setKey("livedata");
        t.setTitle("Live-Daten");
        t.setIcon("livefeed");
        t.setMainTab(false);
//        t.getFilterEntries().add(textFilter);
        list.add(t);

        t = new TabConfig();
        t.setKey("dashboard");
        t.setTitle("Aufträge");
        t.setIcon("job");
        t.setMainTab(true);
        t.getFilterEntries().add(textFilter);
        t.getFilterEntries().add(statusFilter);
        t.getSortEntries().add(stateSort);
        t.getSortEntries().add(dateSort);
        t.getSortEntries().add(typeSort);
        list.add(t);

        t = new TabConfig();
        t.setKey("actions");
        t.setTitle("Aktionen");
        t.setIcon("action");
        t.setMainTab(false);
        t.getFilterEntries().add(textFilter);
        t.getFilterEntries().add(statusFilter);
        t.getSortEntries().add(stateSort);
        t.getSortEntries().add(dateSort);
        t.getSortEntries().add(typeSort);
        list.add(t);

        return list;
    }
}
