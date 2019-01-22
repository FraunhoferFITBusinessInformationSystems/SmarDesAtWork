/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.builders;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.enums.JobType;
import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;

/**
 * Created by vh on 01.03.2018.
 */

public class JobBuilder implements Builder<Job>{

    private static final String TAG = "JobBuilder";
    
    ObjectMapper mapper = RestServiceProvider.createJacksonMapper();

    private final Job j;

    public JobBuilder() {
        j = new Job();
        j.setId(UUID.randomUUID());
        j.setCreatedAt(new Date());
    }

    public JobBuilder setCreator(String username){
        j.setCreatedBy(username);
        return this;
    }

    public JobBuilder setReferenceId(UUID referenceId){
        j.setReferenceId(referenceId);
        return this;
    }

    public JobBuilder setJobKey(String jobIdKey){
        j.setName(jobIdKey);
        return this;
    }

    public JobBuilder setJobReplyAction(String action){
        j.setName(action);
        return this;
    }

    public JobBuilder setJobType(JobType jobType){
        j.setType(jobType.toString());
        return this;
    }

//    public JobBuilder copyResources(Map<String, String> resources){
//        return this;
//    }

    public JobBuilder setJobResource(Map<String, String> resources){
        j.setResource(resources);
        return this;
    }

    @Override
    public Job build() {
        return j;
    }
}
