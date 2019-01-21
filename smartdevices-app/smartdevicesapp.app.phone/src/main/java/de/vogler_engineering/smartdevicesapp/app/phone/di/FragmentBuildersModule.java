/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.login.LoginFragment;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs.GenericTabFragment;

/**
 * Created by vh on 21.03.2018.
 */

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract LoginFragment contributeLoginFragment();

    @ContributesAndroidInjector
    abstract GenericTabFragment contributeGenericTabFragment();
}