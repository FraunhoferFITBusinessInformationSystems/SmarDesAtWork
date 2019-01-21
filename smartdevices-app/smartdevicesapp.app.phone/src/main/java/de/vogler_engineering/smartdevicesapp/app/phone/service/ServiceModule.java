/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.service;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;

@Module
public class ServiceModule {

    @Provides
    @Singleton
    public ServiceManager provideServiceManager(AppManager appManager, SchedulersFacade schedulers){
        return new ServiceManager(appManager, schedulers);
    }

}