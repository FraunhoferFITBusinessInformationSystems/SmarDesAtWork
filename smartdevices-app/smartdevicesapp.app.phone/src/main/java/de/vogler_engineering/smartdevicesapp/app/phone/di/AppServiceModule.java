/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.di;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.service.AppFirebaseMessagingService;
import de.vogler_engineering.smartdevicesapp.app.phone.service.UpdateService;

@Module
public abstract class AppServiceModule {

    @ContributesAndroidInjector(modules = AppModule.class)
    abstract UpdateService contributeUpdateService();

    @ContributesAndroidInjector(modules = AppModule.class)
    abstract AppFirebaseMessagingService contributeAppFirebaseMessagingService();

}

