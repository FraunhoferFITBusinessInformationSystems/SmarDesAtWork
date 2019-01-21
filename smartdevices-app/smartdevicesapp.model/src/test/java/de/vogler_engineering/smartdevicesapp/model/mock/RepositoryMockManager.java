/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import android.content.Context;
import android.content.SharedPreferences;

import junit.framework.Assert;

import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DeviceInfoRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MediaRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MessageRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TodoListRepository;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import lombok.Data;
import timber.log.Timber;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

@Data
public class RepositoryMockManager {

    private static final String TAG = "RepositoryMockManager";

    private SchedulersFacadeMock schedulersFacade;
    private AppManager appManager;
    private AppManagerMock appManagerMock;

    private RestServiceProvider restServiceProvider;
    private ConfigRepository configRepository;
    private JobRepository jobRepository;
    private MediaRepository mediaRepository;
    private ResourceRepository resourceRepository;
    private DeviceInfoRepository deviceInfoRepository;
    private TabRepository tabRepository;
    private MessageRepository messageRepository;
    private TodoListRepository todoListRepository;

    private SharedPreferences sharedPrefs;
    private Context context;

    private Properties testProperties = new Properties();

    public RepositoryMockManager() {
    }

    public void init() {
        setUpContext();
        setUpProperties();
        setUpSchedulers();
        setUpAppManager();
        setUpPreferences();
        setUpRepositories();
    }

    private void setUpProperties() {
        File file = new File("integrationtest.properties");
        if(!file.exists()){
            file = new File("../integrationtest.properties");
        }
        Assert.assertTrue("Loading IntegrationTest properties", file.exists());
        try {
            testProperties.load(new FileReader(file));
        } catch (IOException e) {
            Assert.fail("IOException: " + e.getMessage());
        }
    }

    public void setUpContext(){
        try{
        context = Mockito.mock(Context.class);
        }catch(Exception e){
            Timber.tag(TAG).i(e, "Could not initialize Context with Mockito!");
        }
    }

    public void setUpAppManager(){
        appManagerMock = new AppManagerMock();
        appManager = appManagerMock;
        appManager.setMainContext(context);

        appManagerMock.baseUrl.postValue(
                testProperties.getProperty("integrationtest.server.name") + ":" +
                testProperties.getProperty("integrationtest.server.port")
        );
        appManagerMock.deviceId.postValue(
                new DeviceId(testProperties.getProperty("integrationtest.server.deviceId")));
    }

    public void setUpSchedulers(){
        schedulersFacade = new SchedulersFacadeMock();
        schedulersFacade.initializeTestDefaults();
    }

    public void setUpPreferences(){
        //Setup Shared Preferences
        try {
            this.sharedPrefs = Mockito.mock(SharedPreferences.class);
            Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
            appManagerMock.sharedPrefs = this.sharedPrefs;
        }catch(Exception e){
            Timber.tag(TAG).i(e, "Could not initialize Preferences with Mockito!");
        }
    }

    public void setUpRepositories(){
        restServiceProvider = new RestServiceProvider(appManager);
        configRepository = new ConfigRepository(appManager, schedulersFacade, restServiceProvider);
        tabRepository = new OfflineTabRepositoryMock(appManager, schedulersFacade, restServiceProvider, configRepository);
        jobRepository = new JobRepository(appManager, schedulersFacade, restServiceProvider, tabRepository);
        mediaRepository = new MediaRepository(appManager, schedulersFacade, restServiceProvider);
        resourceRepository = new ResourceRepositoryMock(appManager, schedulersFacade, restServiceProvider);
        deviceInfoRepository = new DeviceInfoRepository(appManager, schedulersFacade, configRepository);
        messageRepository = new MessageRepository(appManager, schedulersFacade, restServiceProvider);
        todoListRepository = new TodoListRepository(appManager, schedulersFacade, restServiceProvider);
    }
}
