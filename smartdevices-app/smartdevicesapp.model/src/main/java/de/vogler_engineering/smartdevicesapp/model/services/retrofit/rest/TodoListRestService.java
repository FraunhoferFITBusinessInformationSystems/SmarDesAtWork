/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest;

import java.util.List;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListDetails;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListHeader;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListInstanceDto;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListInstanceHeaderDto;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by vh on 14.02.2018.
 */

public interface TodoListRestService {

    @GET("api/todo/lists")
    Single<List<TodoListHeader>> getAllListDetails(@Query("context") String context);

    @GET("api/todo/lists/{key}")
    Single<TodoListDetails> getListDetails(@Path("key") String key, @Query("context") String context);

    @GET("api/todo/instances")
    Single<List<TodoListInstanceHeaderDto>> getAllInstances(@Query("context") String context);

    @GET("api/todo/instances/{id}")
    Single<TodoListInstanceDto> getInstance(@Path("id") UUID id);

    @PUT("api/todo/instances")
    Single<UUID> startInstance(@Query("key") String listKey, @Query("context") String contextDomain, @Query("device") DeviceId device, @Body Object context);

    @PUT("api/todo/instances/{id}/{step}/{state}")
    Single<Integer> confirmStep(@Path("id") UUID id, @Path("step") int step, @Path("state") boolean state, @Query("device") DeviceId device);

    @DELETE("api/todo/instances/{id}")
    Single<Boolean> closeInstance(@Path("id") UUID id, @Query("device") DeviceId device);
}
