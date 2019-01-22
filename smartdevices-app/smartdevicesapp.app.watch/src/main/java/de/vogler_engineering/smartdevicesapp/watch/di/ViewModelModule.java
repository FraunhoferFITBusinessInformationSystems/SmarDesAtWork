/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.vogler_engineering.smartdevicesapp.viewelements.viewmodel.AppViewModelFactory;
import de.vogler_engineering.smartdevicesapp.watch.ui.job.JobViewModel;
import de.vogler_engineering.smartdevicesapp.watch.ui.main.MainViewModel;
import de.vogler_engineering.smartdevicesapp.watch.ui.settings.SettingsViewModel;

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel.class)
    abstract ViewModel bindSettingsViewModel(SettingsViewModel settingsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindMainViewModel(MainViewModel mainViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(JobViewModel.class)
    abstract ViewModel bindJobViewModel(JobViewModel jobViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory factory);
}
