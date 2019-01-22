/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.manager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.view.View;

import de.vogler_engineering.smartdevicesapp.common.misc.LiteEnumMap;

/**
 * This class takes some ViewElements and supplies Methods to Hide and Show one of these elements,
 * as the particular Loading mState changes.
 * @see ActivityLoadingState
 */
public class ActivityStateManager {

    private final LiveData<ActivityLoadingState> mState;

    private final LiteEnumMap<ActivityLoadingState, View> containers =
            new LiteEnumMap<>(ActivityLoadingState.class);

    public ActivityStateManager(MutableLiveData<ActivityLoadingState> observable) {
        this.mState = observable;
        this.mState.observeForever(this::onLoadingStateChanged);
    }

    public ActivityStateManager(MutableLiveData<ActivityLoadingState> observable, View mainContainer){
        this(observable);
        containers.set(ActivityLoadingState.Active, mainContainer);
    }

    public void setContainer(ActivityLoadingState state, View container){
        containers.set(state, container);
    }

    private void onLoadingStateChanged(ActivityLoadingState newState) {
        View activeView = containers.get(newState);
        if(activeView == null){
            activeView = containers.get(ActivityLoadingState.Active);
        }
        for (ActivityLoadingState state : ActivityLoadingState.values()) {
            setViewContainerState(containers.get(state), activeView == containers.get(state));
        }
    }

    private void setViewContainerState(View view, boolean active){
        if(view != null)
            view.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    public LiveData<ActivityLoadingState> getStateObservable() {
        return mState;
    }

    public ActivityLoadingState getState() {
        return mState.getValue();
    }

    public void updateVisibility() {
        onLoadingStateChanged(mState.getValue());
    }
}
