/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import de.vogler_engineering.smartdevicesapp.model.entities.apk.ApkInfo;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava2.Result;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by vh on 14.02.2018.
 */

public interface ApkRestService {

    @GET("api/apk")
    Single<ApkInfo> getApkInfo();

    @GET("api/apk/phone")
    Single<Result<ResponseBody>> getPhoneApk();

    @GET("api/apk/watch")
    Single<Result<ResponseBody>> getWatchApk();

    @GET("api/apk/{name}")
    Single<Result<ResponseBody>> getApk(@Path("name") String name);

}
