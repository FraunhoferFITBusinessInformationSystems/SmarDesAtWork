/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import de.vogler_engineering.smartdevicesapp.service.entities.auth.JWTResponse;
import de.vogler_engineering.smartdevicesapp.service.entities.auth.OIDCAuthorityInformation;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by vh on 14.02.2018.
 */

public interface AuthRestService {

//    @POST("")
//    Call<JWTResponse> refreshToken();

//    @GET("user")
//    Call<User> getUser(@Header("Authorization") String authorization)

    //https://[server]/auth/realms/smart-des/.well-known/openid-configuration
    @GET("auth/realms/{realm}/.well-known/openid-configuration")
    Observable<OIDCAuthorityInformation> getWellKnown(@Path("realm") String realm);

    //https://[server]/auth/realms/smart-des/protocol/openid-connect/token
    @FormUrlEncoded
    @POST("auth/realms/{realm}/protocol/openid-connect/token")
    Observable<JWTResponse> getToken(
            @Path("realm") String realm,
            @Field("client_id") String first,
            @Field("grant_type") String grantType,
            @Field("scope") String scope,
            @Field("username") String username,
            @Field("password") String password
        );

}
