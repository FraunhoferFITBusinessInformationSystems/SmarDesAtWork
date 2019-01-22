/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.integration;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.util.Log;

import junit.framework.Assert;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import java.util.List;

import de.vogler_engineering.smartdevicesapp.common.test.categories.IntegrationTests;
import de.vogler_engineering.smartdevicesapp.common.test.categories.UnitTests;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListDetails;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListHeader;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListInstanceHeaderDto;
import de.vogler_engineering.smartdevicesapp.model.mock.RepositoryMockManager;
import de.vogler_engineering.smartdevicesapp.model.mock.TimberAssertingOnErrorTestRule;
import de.vogler_engineering.smartdevicesapp.model.repository.TodoListRepository;

@Category(IntegrationTests.class)
public class TodoListApiIntegrationTest extends AbstractIntegrationTest {

    private static final String TAG = "TabUpdateServiceTest";

    RepositoryMockManager rmm;

    @Rule
    public TimberTestRule timberTestRule = TimberTestRule.builder()
            .showThread(true)
            .showTimestamp(true)
            .onlyLogWhenTestFails(false)
            .build();

    @Before
    public void before() throws Exception {
        //Setup Repositories
        rmm = new RepositoryMockManager();
        rmm.init();
    }

    @Test
    public void getListDetailsTest() {
        TodoListRepository repository = rmm.getTodoListRepository();
        final String listKey = "CoffeeDescale";
        final String contextDomain = "TodoSample";
        TodoListDetails todoListDetails = repository.getListDetails(listKey, contextDomain).blockingGet();
        Assert.assertNotNull(todoListDetails);
    }

    @Test
    public void getAllListDetails() {
        TodoListRepository repository = rmm.getTodoListRepository();
        final String contextDomain = "TodoSample";
        List<TodoListHeader> todoListDetails = repository.getAllListDetails(contextDomain).blockingGet();
        Assert.assertNotNull(todoListDetails);
    }

    @Test
    public void getListAllInstancesTest() {
        TodoListRepository repository = rmm.getTodoListRepository();
        final String contextDomain = "TodoSample";
        List<TodoListInstanceHeaderDto> todoListDetails = repository.getAllInstances(contextDomain).blockingGet();
        Assert.assertNotNull(todoListDetails);
    }
}
