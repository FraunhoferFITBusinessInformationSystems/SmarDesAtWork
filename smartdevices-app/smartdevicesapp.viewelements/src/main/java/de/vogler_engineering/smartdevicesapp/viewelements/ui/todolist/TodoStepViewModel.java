/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;

import java.util.UUID;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStep;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepDetails;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepState;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.Navigator;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TodoListRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.AbstractDetailViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutHelpers;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import timber.log.Timber;

public class TodoStepViewModel extends AbstractDetailViewModel {

    private static final String TAG = "TodoStepViewModel";

    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;
    private final TodoListRepository todoListRepository;
    private final Navigator navigator;

    private final MutableLiveData<TodoListStepDetails> details = new MutableLiveData<>();
    private final MutableLiveData<Boolean> requestActive = new MutableLiveData<>();

    private String listId;
    private UUID instanceId;
    private int stepNum;
    private String contextDomain;

    @Inject
    public TodoStepViewModel(AppManager appManager, SchedulersFacade schedulersFacade,
                             JobRepository jobRepository, LayoutHelpers layoutHelpers, TodoListRepository todoListRepository, Navigator navigator) {
        super(schedulersFacade, jobRepository, layoutHelpers);
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
        this.todoListRepository = todoListRepository;
        this.navigator = navigator;
        mLoadingState.setValue(ActivityLoadingState.Loading);
    }

    public UiLayout getUiLayout() {
        return getJobEntry().getUi();
    }

    public MutableLiveData<TodoListStepDetails> getDetailsOverview() {
        return details;
    }

    @Override
    public void finishActivity() {
        super.finishActivity();
    }

    public void loadData(String listId, UUID instanceId, int stepNum, String contextDomain) {
        this.listId = listId;
        this.instanceId = instanceId;
        this.stepNum = stepNum;
        this.contextDomain = contextDomain;
        updateData();
    }

    public void updateData(){
        disposables.add(todoListRepository.getStepDetails(listId, instanceId, stepNum, contextDomain)
                .subscribeOn(schedulersFacade.newThread())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        (listStepDetails) -> {
                            if (details.getValue() == null || !details.getValue().equals(listStepDetails)) {
                                details.postValue(listStepDetails);
                                Timber.tag(TAG).d("Updated stepDetails.");
                            } else {
                                Timber.tag(TAG).i("Did not update stepDetails, no change detected.");
                            }
                            getLoadingFinishedObservable().postValue(null);
                            requestActive.postValue(false);
                        },
                        (err) -> {
                            Timber.tag(TAG).e(err, "Error while updating stepDetails.");
                            requestActive.postValue(false);
                        }
                ));
    }

    public void checkedStatusChanged(Context context, boolean checked) {
        if (requestActive.getValue() == null || !requestActive.getValue()) {
            requestActive.setValue(true);
            try {
                TodoListStepDetails value = details.getValue();
                if (value == null) throw new Exception("Could not read TodoListStepDetails!");
                boolean oldState = (value.getState() == TodoListStepState.Finished);

                disposables.add(todoListRepository.setStepState(value.getInstanceId(), value.getNumber(), checked)
                        .subscribeOn(schedulersFacade.newThread())
                        .observeOn(schedulersFacade.ui())
                        .subscribe(
                                (response) -> {
                                    Timber.tag(TAG).i("Updated TodoListStepState to %s - old %s", checked, oldState);
                                    updateData();
                                },
                                (err) -> {
                                    Timber.tag(TAG).e(err, "Exception while updating TodoListStepState.");
                                    requestActive.postValue(false);
                                }));

                Timber.tag(TAG).i("FinishedClicked!!!!!!");
            } catch (Exception e) {
                Timber.tag(TAG).e("Error while sending");
                requestActive.postValue(false);
            }
        }
    }

    public void nextCardClick(Context context) {
        if (requestActive.getValue() == null || !requestActive.getValue()) {
            requestActive.setValue(true);
            Timber.tag(TAG).i("Navigate to next step");
            TodoListStepDetails value = details.getValue();
            if (value != null) {
                TodoListStep step = value.getNextStep();
                if(step != null) {
                    navigateToStep(context, step, Navigator.ANIMATION_DIRECTION_END);
                    return;
                }
            }
            requestActive.postValue(false);
        }
    }

    public void prevCardClick(Context context) {
        if (requestActive.getValue() == null || !requestActive.getValue()) {
            requestActive.setValue(true);
            Timber.tag(TAG).i("Navigate to previous step");
            TodoListStepDetails value = details.getValue();
            if (value != null) {
                TodoListStep step = value.getPreviousStep();
                if(step != null) {
                    navigateToStep(context, step, Navigator.ANIMATION_DIRECTION_START);
                    return;
                }
            }
            requestActive.postValue(false);
        }
    }

    private void navigateToStep(Context context, TodoListStep step, int direction){
        navigator.navigateToListDetails(context, listId, instanceId, step.getNumber(), contextDomain, direction);
    }

    public LiveData<Boolean> getRequestActiveObservable() {
        return requestActive;
    }
}
