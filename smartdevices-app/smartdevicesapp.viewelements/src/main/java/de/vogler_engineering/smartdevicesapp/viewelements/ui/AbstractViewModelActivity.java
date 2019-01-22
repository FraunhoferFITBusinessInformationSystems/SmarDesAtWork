/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * Created by vh on 08.03.2018.
 */

public abstract class AbstractViewModelActivity<TViewModel extends ViewModel> extends AppCompatActivity implements HasSupportFragmentInjector {

    @Inject
    ViewModelFactory<TViewModel> viewModelFactory;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    protected TViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    protected void setupActivity(Class<TViewModel> clazz, int layoutResource){
        setContentView(layoutResource);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(clazz);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }
}
