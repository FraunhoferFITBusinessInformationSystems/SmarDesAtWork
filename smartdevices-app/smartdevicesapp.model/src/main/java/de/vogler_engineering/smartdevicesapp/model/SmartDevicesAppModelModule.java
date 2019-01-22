/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.db.AppDatabase;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.repository.AuthRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DeviceInfoRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MediaRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MessageRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TodoListRepository;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;

/**
 * Created by vh on 09.02.2018.
 */

@Module
public class SmartDevicesAppModelModule {

    @Provides
    @Singleton
    public RestServiceProvider provideRestServiceProvider(AppManager appManager){
        return new RestServiceProvider(appManager);
    }

    @Provides
    @Singleton
    public AppManagerImpl provideAppManagerImpl(Context appContext){
        return new AppManagerImpl(appContext);
    }

    @Provides
    public AppManager provideAppManager(AppManagerImpl appManager){
        return appManager;
    }

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(Context applicationContext){
        return AppDatabase.createInstance(applicationContext);
    }

    /**  Repositories  ***/

    @Provides
    @Singleton
    public ConfigRepository provideConfigRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider){
        return new ConfigRepository(appManager, schedulersFacade, restServiceProvider);
    }

    @Provides
    @Singleton
    public AuthRepository provideAuthRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, ConfigRepository configRepo){
        return new AuthRepository(appManager, schedulersFacade, restServiceProvider, configRepo);
    }

    @Provides
    @Singleton
    public JobRepository provideJobRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, TabRepository tabRepository){
        return new JobRepository(appManager, schedulersFacade, restServiceProvider, tabRepository);
    }

    @Provides
    @Singleton
    public MessageRepository provideMessageRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider){
        return new MessageRepository(appManager, schedulersFacade, restServiceProvider);
    }

    @Provides
    @Singleton
    public ResourceRepository provideResourceRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider){
        return new ResourceRepository(appManager, schedulersFacade, restServiceProvider);
    }

    @Provides
    @Singleton
    public MediaRepository provideMediaRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider serviceProvider){
        return new MediaRepository(appManager, schedulersFacade, serviceProvider);
    }

    @Provides
    @Singleton
    public DeviceInfoRepository provideDeviceInfoRepository(AppManager appManager, SchedulersFacade schedulersFacade, ConfigRepository configRepository) {
        return new DeviceInfoRepository(appManager, schedulersFacade, configRepository);
    }

    @Provides
    @Singleton
    public DynamicValueRepository provideDynamicValueRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        return new DynamicValueRepository(appManager, schedulersFacade, restServiceProvider);
    }

    @Provides
    @Singleton
    public TabRepository provideMainTabConfigRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, ConfigRepository configRepository) {
        return new TabRepository(appManager, schedulersFacade, restServiceProvider, configRepository);
    }

    @Provides
    @Singleton
    public TodoListRepository provideTodoListRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        return new TodoListRepository(appManager, schedulersFacade, restServiceProvider);
    }
}
