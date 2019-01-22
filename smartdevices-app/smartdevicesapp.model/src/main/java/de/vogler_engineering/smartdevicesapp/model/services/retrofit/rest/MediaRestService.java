/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.media.FileUploadResponse;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava2.Result;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by vh on 14.02.2018.
 */

public interface MediaRestService {

//    @Multipart
//    @PUT("user/photo")
//    Call<User> updateUser(@Part("photo") RequestBody photo, @Part("description") RequestBody description);

//    @Multipart
//    @PUT("api/media/images/{id}")
//    Single<ActionResult> upload(
//            @Part("description") RequestBody description,
//            @Part MultipartBody.Part file
//    );

//    @Multipart
    @POST("api/media/images")
    Single<FileUploadResponse> uploadMedia(@Body MultipartBody requestBody);

//    @Multipart
//    @POST("api/media/images")
//    Single<FileUploadResponse> uploadMedia(@Part("id") String id, @Part("file") MultipartBody requestBody);

//    @Multipart
//    @POST("api/media/images")
//    Single<FileUploadResponse> uploadMedia(@Part MultipartBody.Part body);

    @GET("api/media/images/{id}")
    Single<Result<ResponseBody>> getMedia(@Path("id") UUID id);
}
