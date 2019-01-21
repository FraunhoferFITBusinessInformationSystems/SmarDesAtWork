/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by vh on 08.03.2018.
 */

public abstract class AbstractViewModelFragment<TViewModel extends ViewModel> extends AbstractFragment {

    @Inject
    ViewModelFactory<TViewModel> viewModelFactory;

    protected TViewModel viewModel;

    protected <TFragment extends AbstractViewModelFragment> View inflateFragment(
            TFragment fragment,
            int layoutResource,
            LayoutInflater inflater,
            ViewGroup container,
            Class<TViewModel> viewModelClazz){
        // Inflate the layout for this fragment
        View view = inflater.inflate(layoutResource, container, false);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(viewModelClazz);
        ButterKnife.bind(fragment, view);
        return view;
    }

    protected <TFragment extends AbstractViewModelFragment> View inflateFragment(
            TFragment fragment,
            Fragment viewModelScope,
            int layoutResource,
            LayoutInflater inflater,
            ViewGroup container,
            Class<TViewModel> viewModelClazz){
        // Inflate the layout for this fragment
        View view = inflater.inflate(layoutResource, container, false);
        viewModel = ViewModelProviders.of(viewModelScope, viewModelFactory).get(viewModelClazz);
        ButterKnife.bind(fragment, view);
        return view;
    }

    public TViewModel getViewModel() {
        return viewModel;
    }
}
