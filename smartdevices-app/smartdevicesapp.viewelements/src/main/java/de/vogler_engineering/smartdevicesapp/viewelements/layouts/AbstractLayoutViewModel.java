/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import androidx.lifecycle.ViewModel;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public abstract class AbstractLayoutViewModel extends ViewModel implements LayoutViewModelReference {

    protected final ViewModelReference viewModelDelegate;

    public AbstractLayoutViewModel(ViewModelReference viewModelDelegate) {
        this.viewModelDelegate = viewModelDelegate;
    }

    abstract Single<Boolean> initializeAsync(JobEntryDto jobEntry);

    @Override
    public void addDisposable(Disposable disposable) {
        viewModelDelegate.addDisposable(disposable);
    }

    @Override
    public void postLoadingState(ActivityLoadingState state) {
        viewModelDelegate.postLoadingState(state);
    }

    @Override
    public ActivityLoadingState getLoadingState() {
        return viewModelDelegate.getLoadingState();
    }

    @Override
    public void finishActivity() {
        viewModelDelegate.finishActivity();
    }

    @Override
    public void updateData() {
    }

    public String getContextDomain(){
        return null;
    }

    public void onBackPressed() {
    }
}
