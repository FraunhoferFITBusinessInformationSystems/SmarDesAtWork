/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.vogler_engineering.smartdevicesapp.app.phone.App;
import de.vogler_engineering.smartdevicesapp.app.phone.service.ServiceManager;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.management.Navigator;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;

/**
 * Created by vh on 09.02.2018.
 */

@Module(includes = ViewModelModule.class)
class AppModule {

    @Provides
    public Context provideContext(App application) {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public NavigationController provideNavigationController(AppManager appManager){
        return new NavigationController(appManager);
    }

    @Provides
    @Singleton
    public Navigator provideNavigator(NavigationController navigationController){
        return navigationController;
    }

    @Provides
    public BaseServiceManager provideServiceManager(AppManager appManager, SchedulersFacade schedulers){
        return new ServiceManager(appManager, schedulers);
    }

    @Provides
    public UiMessageUtil provideUiMessageUtil(AppManager appManager, SchedulersFacade schedulersFacade){
        return new UiMessageUtil(appManager, schedulersFacade);
    }
}
