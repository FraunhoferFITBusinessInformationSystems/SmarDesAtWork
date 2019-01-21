/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.Nullable;
import de.vogler_engineering.smartdevicesapp.common.util.DateUtils;
import de.vogler_engineering.smartdevicesapp.common.util.StringUtil;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.interceptor.LoggingInterceptor;
import lombok.Getter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import timber.log.Timber;

/**
 * Created by vh on 19.02.2018.
 */

@Singleton
public class RestServiceProvider {

    private static final String TAG = "RestServiceProvider";

    @Getter
    private final AppManager appManager;

    private boolean configChanged = false;

    private OkHttpClient httpClient;
    private Retrofit retrofit;
    private Picasso picasso = null;

    @Inject
    public RestServiceProvider(AppManager appManager){
        this.appManager = appManager;

        this.appManager.getBaseUrlObservable().observeForever((s) -> onServerSettingsChanged());
        this.appManager.getDeviceIdObservable().observeForever((s) -> onServerSettingsChanged());
    }

    private void onServerSettingsChanged() {
        configChanged = true;
    }

    protected Retrofit createServices(){
        return createServices(appManager.getBaseUrl());
    }

    protected Retrofit createServices(String baseUrl){
        if(baseUrl == null || baseUrl.trim().isEmpty()){
            return null;
        }

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.readTimeout(5, TimeUnit.SECONDS);

        //TODO TokenHandler and Access/Refreshtoken management
//        if(authRepo.singedIn.get())
//            httpClient.addInterceptor(new AuthInterceptor(appManager.getAuthToken().getAccessToken()));

        if(appManager.isLogRetrofit())
            httpClientBuilder.addNetworkInterceptor(new LoggingInterceptor());

        httpClient = httpClientBuilder.build();

        Retrofit.Builder retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(createJacksonMapper()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient);

        return retrofit.build();
    }

    public static ObjectMapper createJacksonMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(DateUtils.getJsonDateFormat());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Nullable
    public Retrofit getRetrofit(){
        if(httpClient == null || retrofit == null || configChanged) {
//            retrofit = createServices();
//            configChanged = false;
            try {
                retrofit = createServices();
                configChanged = false;
            }catch(Exception e){
                Timber.tag(TAG).w(e, "Could not create service with URL: %s", appManager.getBaseUrl());
            }
        }
        return retrofit;
    }

    @Nullable
    public OkHttpClient getHttpClient() {
        if(httpClient == null || configChanged) {
//            retrofit = createServices();
//            configChanged = false;
            try {
                retrofit = createServices();
                configChanged = false;
            }catch(Exception e){
                Timber.tag(TAG).w(e, "Could not create service with URL: %s", appManager.getBaseUrl());
            }
        }
        return httpClient;
    }

    public <T> T createRestService(Class<T> clazz){
        Retrofit rf = getRetrofit();
        if(rf == null) throw new RuntimeException("HttpService not ready!");
        return rf.create(clazz);
    }

    public <T> T createRestService(Class<T> clazz, String baseUrl){
        return createServices(baseUrl).create(clazz);
    }
}
