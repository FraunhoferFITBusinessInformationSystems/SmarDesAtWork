/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by vh on 19.02.2018.
 */

public class LoggingInterceptor implements Interceptor {

    private final static String TAG = "RetrofitLogInterceptor";

    private final static boolean ADVANCED_LOGGING = true;

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        long t1 = System.nanoTime();
        Timber.tag(TAG).d("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers());
        if(ADVANCED_LOGGING){
            //Timber.tag(TAG).d("Body: %s\nHeader: %s", request.body().toString(), request.headers().toString());
        }

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Timber.tag(TAG).d("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers());

        return response;
    }
}
