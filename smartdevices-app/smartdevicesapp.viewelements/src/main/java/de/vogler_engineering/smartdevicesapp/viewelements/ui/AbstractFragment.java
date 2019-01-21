/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;

public abstract class AbstractFragment<VM extends AbstractViewModel> extends Fragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    protected VM viewModel = null;

    protected void loadViewModel(Class<VM> clazz){
        if(viewModel != null)
            return;
        if(viewModelFactory == null)
            throw new RuntimeException("No viewmodel factory injected!");
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(clazz);
    }

    protected void loadViewModel(Class<VM> clazz, Fragment scope){
        if(viewModel != null)
            return;
        if(viewModelFactory == null)
            throw new RuntimeException("No viewmodel factory injected!");
        viewModel = ViewModelProviders.of(scope, viewModelFactory).get(clazz);
    }

    protected void loadViewModel(Class<VM> clazz, FragmentActivity scope){
        if(viewModel != null)
            return;
        viewModel = ViewModelProviders.of(scope, viewModelFactory).get(clazz);
    }

    protected ViewModelProvider.Factory getViewModelFactory() {
        return viewModelFactory;
    }

    protected <TFragment extends AbstractFragment> View inflateFragmentLayout(TFragment fragment, int layoutResource, LayoutInflater inflater, @Nullable ViewGroup container){
        View view = inflater.inflate(layoutResource, container, false);
        ButterKnife.bind(fragment, view);
        return view;
    }
}
