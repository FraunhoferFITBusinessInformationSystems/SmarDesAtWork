/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.unittests;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.util.Log;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;

import de.vogler_engineering.smartdevicesapp.common.test.categories.UnitTests;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.mock.RepositoryMockManager;
import de.vogler_engineering.smartdevicesapp.model.mock.TimberAssertingOnErrorTestRule;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;

@Category(UnitTests.class)
public class TabRepositoryUnitTest {

    private static final String TAG = "TabUpdateServiceTest";

    RepositoryMockManager rmm;

    @Rule
    public TimberTestRule timberTestRule = TimberTestRule.builder()
            .showThread(true)
            .showTimestamp(true)
            .onlyLogWhenTestFails(false)
            .build();

    @Rule
    public TestRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public TimberAssertingOnErrorTestRule assertingOnErrorTestRule =
            TimberAssertingOnErrorTestRule.builder().minAssertPriority(Log.ERROR).build();

    @Before
    public void before() throws Exception {
        //Setup Repositories
        rmm = new RepositoryMockManager();
        rmm.init();
    }

    @Test
    public void testTabUpdateInsertAndRemove() {
        TabRepository tabRepository = new TabRepository(rmm.getAppManager(),
                rmm.getSchedulersFacade(), rmm.getRestServiceProvider(), rmm.getConfigRepository());

        //Init with 2 tabs (add 2 tabs)
        TabConfig[] t1 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l1 = asArrayList( t1[0], t1[1] );
        tabRepository.configChanged(l1);
        assertTabConfigLists(l1, tabRepository.getTabConfig());

        //Add tab at the end
        TabConfig[] t2 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l2 = asArrayList( t2[0], t2[1], t2[2]);
        tabRepository.configChanged(l2);
        assertTabConfigLists(l2, tabRepository.getTabConfig());

        //Add tab in the middle
        TabConfig[] t21 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l21 = asArrayList( t21[0], t21[1], t21[3], t21[2]);
        tabRepository.configChanged(l21);
        assertTabConfigLists(l21, tabRepository.getTabConfig());

        //Add tab at the beginning
        TabConfig[] t22 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l22 = asArrayList( t22[4], t22[0], t22[1], t22[3], t22[2]);
        tabRepository.configChanged(l22);
        assertTabConfigLists(l22, tabRepository.getTabConfig());

        //Remove tab at the end
        TabConfig[] t4 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l4 = asArrayList(  t4[4], t4[0], t4[1], t4[3] );
        tabRepository.configChanged(l4);
        assertTabConfigLists(l4, tabRepository.getTabConfig());

        //Remove tab in the middle
        TabConfig[] t41 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l41 = asArrayList( t41[4], t41[0], t41[3]);
        tabRepository.configChanged(l41);
        assertTabConfigLists(l41, tabRepository.getTabConfig());

        //Remove tab at the beginning
        TabConfig[] t42 = createSampleTabConfigs(5);
        ArrayList<TabConfig> l42 = asArrayList(  t42[0], t42[3] );
        tabRepository.configChanged(l42);
        assertTabConfigLists(l42, tabRepository.getTabConfig());

        //Remove all tabs
        ArrayList<TabConfig> l7 = new ArrayList<>();
        tabRepository.configChanged(l7);
        assertTabConfigLists(l7, tabRepository.getTabConfig());
    }

    @Test
    public void testTabUpdateOrder() {
        TabRepository tabRepository = new TabRepository(rmm.getAppManager(),
                rmm.getSchedulersFacade(), rmm.getRestServiceProvider(), rmm.getConfigRepository());

        TabConfig[] t = createSampleTabConfigs(5);
        ArrayList<TabConfig> l1 = asArrayList(t[0], t[1], t[2], t[3], t[4]);
        tabRepository.configChanged(l1);
        assertTabConfigLists(l1, tabRepository.getTabConfig());

        ArrayList<TabConfig> l2 = asArrayList(t[0], t[2], t[1], t[3], t[4]);
        tabRepository.configChanged(l2);
        assertTabConfigLists(l2, tabRepository.getTabConfig());

        ArrayList<TabConfig> l3 = asArrayList(t[0], t[2], t[4], t[1], t[3]);
        tabRepository.configChanged(l3);
        assertTabConfigLists(l3, tabRepository.getTabConfig());

        ArrayList<TabConfig> l4 = asArrayList(t[4], t[3], t[2], t[1], t[0]);
        tabRepository.configChanged(l4);
        assertTabConfigLists(l4, tabRepository.getTabConfig());
    }

    @Test
    public void testTabUpdateTabChange() {
        TabRepository tabRepository = new TabRepository(rmm.getAppManager(),
                rmm.getSchedulersFacade(), rmm.getRestServiceProvider(), rmm.getConfigRepository());

        //Init with 2 tabs (add 2 tabs)
        TabConfig[] t1 = createSampleTabConfigs(3);
        ArrayList<TabConfig> l1 = asArrayList(t1[0], t1[1]);
        tabRepository.configChanged(l1);
        assertTabConfigLists(l1, tabRepository.getTabConfig());

        //Add tab at the end
        TabConfig[] t2 = createSampleTabConfigs(3);
        t2[0].setTitle("Test");
        ArrayList<TabConfig> l2 = asArrayList(t2[0], t2[1], t2[2]);
        tabRepository.configChanged(l2);
        assertTabConfigLists(l2, tabRepository.getTabConfig());
    }

    private void assertTabConfigLists(ArrayList<TabConfig> expected, ArrayList<TabConfig> actual){
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);
        Assert.assertEquals("List size", expected.size(), actual.size());
        for(int i = 0; i < expected.size(); i++){
            TabConfig e = expected.get(i);
            TabConfig a = expected.get(i);
            Assert.assertNotNull(e);
            Assert.assertNotNull(a);
            Assert.assertEquals("TabConfig.Key", e.getKey(), a.getKey());
            Assert.assertEquals("TabConfig.Title", e.getTitle(), a.getTitle());
            Assert.assertEquals("TabConfig.MainTab", e.isMainTab(), a.isMainTab());
        }
    }

    private TabConfig[] createSampleTabConfigs(int count){
        TabConfig[] c = new TabConfig[count];
        for(int i = 0; i < count; i++){
            c[i] = new TabConfig();
            c[i].setKey("t"+i);
            c[i].setTitle("T"+i);
            c[i].setMainTab(i==1);
        }
        return c;
    }

    private <T> ArrayList<T> asArrayList(T ... params){
        return new ArrayList<>(Arrays.asList(params));
    }

}
