/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import java.util.List;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.TabEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueData;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by vh on 14.02.2018.
 */

public interface DataRestService {

    @GET("api/data/{deviceId}/values")
    Single<List<DynamicValueData>> getDynamicValueData(@Path("deviceId") String deviceId);

//    @GET("api/data/tab/{deviceId}/{tab}")
//    Single<Map<String, List<TabEntryDto>>> getTabDataShort(@Path("deviceId") String deviceId,
//                                              @Path("tab") String tabKey);

    @GET("api/data/tab/{deviceId}/{tab}")
    Single<List<TabEntryDto>> getTabDataShort(@Path("deviceId") String deviceId,
                                              @Path("tab") String tabKey,
                                              @QueryMap Map<String, String> paginationRequest,
                                              @QueryMap Map<String, String> filterRequest,
                                              @QueryMap Map<String, String> sortRequest);

}
