/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStep;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import timber.log.Timber;

public class ToggleableEntryComponentData extends ComponentData<TodoListStep>{

    private static final String TAG = "ToggleableEntryComponentData";

    private MutableLiveData<Boolean> finished = new MutableLiveData<>();

    public ToggleableEntryComponentData(UiComponent element, ConfigurableViewModelFeatures features) {
        super(element, features);
    }

    @Override
    public void setResourceValue(String s) {

    }

    @Override
    public String getResourceValue() {
        return null;
    }

    public boolean isFinished() {
        return finished.getValue() != null ? finished.getValue() : false;
    }

    public LiveData<Boolean> getFinishedObservable() {
        return finished;
    }

    public void onClicked() {
//        finished.postValue(true);
        TodoListStep value = this.value.getValue();
        if(value != null && value.getParent() != null) {
            int num = value.getNumber();
            UUID instanceId = value.getParent().getInstanceId();
            String listId = value.getParent().getId();

            Timber.tag(TAG).i("Opening TodoListDetails with Key:%s, Id:%s, Step:%d", listId, instanceId, num);
            features.openTodoListDetails(listId, instanceId, num);
        }else{
            Timber.tag(TAG).e("Could not open TodoListDetails. Value not set!");
        }

        //        features.startActivity();

        //
//        TODO
//        UUID id = getValue().getId();
//        String key = component.getName();
//        if(id != null) {
//            Job job = value.getValue();
//
//            if(job != null && job.getStatus() != JobStatus.Done) {
//                Timber.tag(TAG).i("Opening Job: %s", id.toString());
//                features.openJob(job.getName(), job.getId());
//            }
//        } else {
//            Timber.tag(TAG).e("Cannot open job, ID unknown.");
//        }

    }
}
