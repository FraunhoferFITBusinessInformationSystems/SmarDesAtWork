/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.ActionResult;
import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.entities.message.MessageReply;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ActionsRestService {

    @PUT("api/actions/jobs/{deviceId}")
    Single<ActionResult> putJob(@Path("deviceId") String deviceId,
                                @Body Job jobReply);

    @GET("api/actions/jobs/{deviceId}/clearjobs")
    Single<ActionResult> clearDeviceJobs(@Path("deviceId") String deviceId);

    @PUT("api/actions/messages/{deviceId}")
    Single<ActionResult> putMessageReply(@Path("deviceId") String deviceId,
                                         @Body MessageReply reply);
}
