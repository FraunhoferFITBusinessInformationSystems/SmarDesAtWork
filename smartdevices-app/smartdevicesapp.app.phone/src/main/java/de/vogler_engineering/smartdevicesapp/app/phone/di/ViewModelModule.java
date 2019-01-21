/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.job.JobViewModel;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.login.LoginViewModel;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.settings.SettingsViewModel;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs.GenericTabViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist.TodoStepViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.viewmodel.AppViewModelFactory;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel.class)
    abstract ViewModel bindSettingsViewModel(SettingsViewModel settingsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(JobViewModel.class)
    abstract ViewModel bindJobViewModel(JobViewModel jobViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(GenericTabViewModel.class)
    abstract ViewModel bindGenericTabViewModel(GenericTabViewModel genericTabViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TodoStepViewModel.class)
    abstract ViewModel bindTodoStepViewModel(TodoStepViewModel todoStepViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory factory);
}
