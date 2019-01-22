/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import io.reactivex.Single;
import retrofit2.adapter.rxjava2.Result;
import retrofit2.http.GET;

/**
 * Created by vh on 14.02.2018.
 */

public interface EchoRestService {

    @GET("api/echo")
    Single<Result<String>> getEcho();

}
