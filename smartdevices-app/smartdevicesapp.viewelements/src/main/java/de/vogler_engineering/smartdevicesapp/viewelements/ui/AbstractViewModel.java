/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by vh on 13.03.2018.
 */

public abstract class AbstractViewModel extends ViewModel {

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    AppManager appManager;

    private MutableLiveData<Void> mActivityFinishedObservable = new MutableLiveData<>();

    protected final CompositeDisposable disposables = new CompositeDisposable();

    protected AbstractViewModel() {
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public MutableLiveData<Void> getActivityFinishedObservable() {
        return mActivityFinishedObservable;
    }

    protected void finishActivity(){
        mActivityFinishedObservable.postValue(null);
    }
}
