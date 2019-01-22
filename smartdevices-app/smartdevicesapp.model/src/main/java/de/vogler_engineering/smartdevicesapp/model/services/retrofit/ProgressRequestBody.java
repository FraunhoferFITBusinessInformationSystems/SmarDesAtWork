/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.retrofit;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by vh on 08.03.2018.
 */

public class ProgressRequestBody extends RequestBody {

    private final static int DEFAULT_BUFFER_SIZE = 2048;

    private final File mFile;
    private final MediaType mediaType;
    private int ignoreFirstNumberOfWriteToCalls;
    private int numWriteToCalls;

    private PublishSubject<Float> progressSubject = PublishSubject.create();

    public ProgressRequestBody(File file, MediaType mediaType){
        this.mFile = file;
        this.mediaType = mediaType;
        this.ignoreFirstNumberOfWriteToCalls = 0;
    }

    public ProgressRequestBody(File file, MediaType mediaType, int ignoreFirstNumberOfWriteToCalls){
        this.mFile = file;
        this.mediaType = mediaType;
        this.ignoreFirstNumberOfWriteToCalls = ignoreFirstNumberOfWriteToCalls;
    }


    public Observable<Float> getProgressSubject() {
        return progressSubject;
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        numWriteToCalls++;
        long fileLength = mFile.length();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        InputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try{
            int read;
            float lastProgressPercentUpdate = 0.0f;

            read = in.read(buffer);
            while(read != -1){
                uploaded += read;
                sink.write(buffer, 0, read);
                read = in.read(buffer);

                // when using HttpLoggingInterceptor it calls writeTo and passes data into a local buffer just for logging purposes.
                // the second call to write to is the progress we actually want to track
                if (numWriteToCalls > ignoreFirstNumberOfWriteToCalls ) {
                    float progress = (uploaded / fileLength) * 100f;
                    if(progress - lastProgressPercentUpdate > 1 || progress == 100f){
                        progressSubject.onNext(progress);
                        lastProgressPercentUpdate = progress;
                    }
                }
            }
        }finally {
            in.close();
        }
    }
}
