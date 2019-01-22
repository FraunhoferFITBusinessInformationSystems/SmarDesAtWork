/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;

public abstract class AbstractRepository {

    protected final AppManager appManager;
    protected final SchedulersFacade schedulersFacade;

    protected final CompositeDisposable disposables = new CompositeDisposable();

    public AbstractRepository(AppManager appManager, SchedulersFacade schedulersFacade) {
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
    }

    public File generateDownloadTempFile(String name) throws IOException {
        File storageDir = appManager.getMainContext().getCacheDir();
        String id = UUID.randomUUID().toString();
        return new File(storageDir, String.format("res-%s-%s.tmp", name, id));
    }

    public File generateDownloadImageFile(UUID id, String suffix) throws IOException {
        File storageDir = appManager.getMainContext().getExternalFilesDir("Pictures/Received");
        return new File(storageDir, id.toString() + suffix);
    }

    public static boolean writeResponseBody(ResponseBody body, File file) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                //long fileSize = body.contentLength();
                //long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    //fileSizeDownloaded += read;
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    public void updateData(){
    }

    public void updateConfig(){
    }
}
