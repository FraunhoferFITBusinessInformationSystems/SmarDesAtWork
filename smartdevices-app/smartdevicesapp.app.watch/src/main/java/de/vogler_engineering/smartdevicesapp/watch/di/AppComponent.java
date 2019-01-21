/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.di;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import de.vogler_engineering.smartdevicesapp.common.SmartDevicesAppCommonModule;
import de.vogler_engineering.smartdevicesapp.model.SmartDevicesAppModelModule;
import de.vogler_engineering.smartdevicesapp.watch.App;

/**
 * Created by vh on 09.02.2018.
 */

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        BuildersModule.class,
        SmartDevicesAppCommonModule.class,
        SmartDevicesAppModelModule.class})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(App application);
        AppComponent build();
    }
    void inject(App app);
}