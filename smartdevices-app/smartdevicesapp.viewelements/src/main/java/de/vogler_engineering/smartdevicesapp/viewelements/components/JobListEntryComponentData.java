/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.entities.job.JobStatus;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import timber.log.Timber;

public class JobListEntryComponentData extends ComponentData<Job>{

    private static final String TAG = "JobListEntryComponentData";

    public JobListEntryComponentData(UiComponent element, ConfigurableViewModelFeatures features) {
        super(element, features);
    }

    @Override
    public void setResourceValue(String s) {

    }

    @Override
    public String getResourceValue() {
        return null;
    }

    public void onClicked() {
        UUID id = getValue().getId();
        String key = component.getName();
        if(id != null) {
            Job job = value.getValue();

            //TODO Workaround remove this!
            if(job != null && job.getStatus() != JobStatus.Done) {
                Timber.tag(TAG).i("Opening Job: %s", id.toString());
                features.openJob(job.getName(), job.getId());
            }
        } else {
            Timber.tag(TAG).e("Cannot open job, ID unknown.");
        }

    }
}
