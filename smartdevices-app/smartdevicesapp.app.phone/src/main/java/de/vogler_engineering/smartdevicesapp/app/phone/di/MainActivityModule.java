/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.job.JobActivity;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.main.MainActivity;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.settings.SettingsActivity;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.todostep.TodoStepActivity;

/**
 * Created by vh on 21.03.2018.
 */

@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract SettingsActivity contributeSettingsActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract JobActivity contributeJobActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract TodoStepActivity contributeTodoStepActivity();
}
