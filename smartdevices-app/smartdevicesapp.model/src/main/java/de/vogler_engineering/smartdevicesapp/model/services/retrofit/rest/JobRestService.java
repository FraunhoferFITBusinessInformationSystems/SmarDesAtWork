/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import io.reactivex.Single;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface JobRestService {

    @GET("api/jobs")
    Single<Map<DeviceId, List<JobEntryDto>>> getAllJobs(@QueryMap Map<String, String> filterRequest,
                                                        @QueryMap Map<String, String> sortRequest);

    @GET("api/jobs/{deviceId}")
    Single<List<JobEntryDto>> getJobs(@Path("deviceId") String deviceIdKey,
                                      @QueryMap Map<String, String> paginationRequest,
                                      @QueryMap Map<String, String> sortRequest,
                                      @QueryMap Map<String, String> filterRequest);

    @GET("api/jobs/{deviceId}/{jobId}")
    Single<JobEntryDto> getJob(@Path("deviceId") String deviceIdKey,
                               @Path("jobId") UUID jobId);

    @DELETE("api/jobs/{deviceId}/{jobId}")
    Single<Boolean> removeJob(@Path("deviceId") String deviceIdKey,
                              @Path("jobId") UUID jobId);
}
