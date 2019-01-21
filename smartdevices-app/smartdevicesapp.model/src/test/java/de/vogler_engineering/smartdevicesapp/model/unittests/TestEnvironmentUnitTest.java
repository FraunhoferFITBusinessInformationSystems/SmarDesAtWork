/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.unittests;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.util.Log;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import de.vogler_engineering.smartdevicesapp.common.test.categories.UnitTests;
import de.vogler_engineering.smartdevicesapp.model.mock.RepositoryMockManager;
import de.vogler_engineering.smartdevicesapp.model.mock.TimberAssertingOnErrorTestRule;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import timber.log.Timber;

import static junit.framework.Assert.assertNotNull;

@Category(UnitTests.class)
public class TestEnvironmentUnitTest {

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
    public void testTestSuite(){
        assertNotNull(rmm.getAppManager());
        assertNotNull(rmm.getSchedulersFacade());
        assertNotNull(rmm.getRestServiceProvider());

        assertNotNull(rmm.getConfigRepository());
        assertNotNull(rmm.getTabRepository());
        assertNotNull(rmm.getJobRepository());
        assertNotNull(rmm.getMediaRepository());
        assertNotNull(rmm.getResourceRepository());
        assertNotNull(rmm.getDeviceInfoRepository());
    }
}
