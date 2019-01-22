/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceConfig;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceInfo;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.ServerInfo;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by vh on 14.02.2018.
 */

public interface ConfigRestService {

    @GET("api/config/{deviceId}")
    Single<DeviceConfig> getDeviceConfig(@Path("deviceId") String deviceId);

    @GET("api/config/info/{deviceId}")
    Single<DeviceInfo> getDeviceInfo(@Path("deviceId") String deviceId);

    @PUT("api/config/info/{deviceId}")
    Single<DeviceInfo> putDeviceInfo(@Path("deviceId") String deviceId, @Body DeviceInfo deviceInfo);

    @GET("api/config/info/server")
    Single<ServerInfo> getServerInfo();
}
