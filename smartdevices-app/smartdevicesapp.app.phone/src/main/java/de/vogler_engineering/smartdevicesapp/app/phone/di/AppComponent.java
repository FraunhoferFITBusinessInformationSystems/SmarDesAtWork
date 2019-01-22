/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.di;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import de.vogler_engineering.smartdevicesapp.app.phone.App;
import de.vogler_engineering.smartdevicesapp.common.SmartDevicesAppCommonModule;
import de.vogler_engineering.smartdevicesapp.model.SmartDevicesAppModelModule;
import de.vogler_engineering.smartdevicesapp.viewelements.SmartDevicesAppViewElementsModule;

/**
 * Created by vh on 09.02.2018.
 */

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,
        AppServiceModule.class,
        MainActivityModule.class,
        SmartDevicesAppCommonModule.class,
        SmartDevicesAppModelModule.class,
        SmartDevicesAppViewElementsModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(App application);

        AppComponent build();
    }

    void inject(App app);
}