/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.vogler_engineering.smartdevicesapp.watch.ui.job.JobActivity;
import de.vogler_engineering.smartdevicesapp.watch.ui.main.MainActivity;
import de.vogler_engineering.smartdevicesapp.watch.ui.settings.SettingsActivity;
import de.vogler_engineering.smartdevicesapp.watch.service.AppFirebaseMessagingService;
import de.vogler_engineering.smartdevicesapp.watch.service.UpdateService;


@Module
public abstract class BuildersModule {

    /// Main Module ///

    @ContributesAndroidInjector()
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector()
    abstract SettingsActivity bindSettingsActivity();

    @ContributesAndroidInjector()
    abstract JobActivity bindDetailActivity();

    @ContributesAndroidInjector()
    abstract AppFirebaseMessagingService bindSmartdeviceFirebaseMessagingService();

    @ContributesAndroidInjector()
    abstract UpdateService bindUpdateService();

}
