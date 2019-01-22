/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.job;

import de.vogler_engineering.smartdevicesapp.model.R;

/**
 * Created by vh on 25.02.2018.
 */

public enum JobStatus {
    Created(R.string.job_status_enum_created),
    Received(R.string.job_status_enum_received),
    Started(R.string.job_status_enum_started),
    InProgress(R.string.job_status_enum_in_progress),
    Done(R.string.job_status_enum_done),
    Closed(R.string.job_status_enum_closed);

    public final int job_status_text_resource;

    JobStatus(int job_status_text_resource) {
        this.job_status_text_resource = job_status_text_resource;
    }
}
