/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.media.FileUploadResponse;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.ProgressRequestBody;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.MediaRestService;
import de.vogler_engineering.smartdevicesapp.model.util.MimeTypeUtils;
import io.reactivex.Single;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by vh on 12.02.2018.
 */

public class MediaRepository extends AbstractRepository {

    private static final String TAG = "MediaRepository";

    private final RestServiceProvider serviceProvider;

    public MediaRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider serviceProvider) {
        super(appManager, schedulersFacade);
        this.serviceProvider = serviceProvider;
    }

//    public void uploadFile(Context context, Uri fileUri) {
//        // create upload service client
//        MediaRestService service =
//                serviceProvider.getRetrofit().create(MediaRestService.class);
//
//        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
//        // use the FileUtils to get the actual file by uri
//        File file = FileUtils.getFile(context, fileUri);
//
//        // create RequestBody instance from file
//        RequestBody requestFile =
//                RequestBody.create(
//                        MediaType.parse(context.getContentResolver().getType(fileUri)),
//                        file
//                );
//
//        // MultipartBody.Part is used to send also the actual file name
//        MultipartBody.Part body =
//                MultipartBody.Part.createFormData("picture", file.getName(), requestFile);
//
//        // add another part within the multipart request
//        String descriptionString = "hello, this is description speaking";
//        RequestBody description =
//                RequestBody.create(
//                        okhttp3.MultipartBody.FORM, descriptionString);
//
//        // finally, execute the request
//        //Call<ResponseBody> call = service.upload(description, body);
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call,
//                                   Response<ResponseBody> response) {
//                Log.v("Upload", "success");
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e("Upload error:", t.getMessage());
//            }
//        });
//    }

    public void uploadFile(Context context, File file, String mediaType) {
        uploadFile(context, file, mediaType, null);
    }

    public Single<FileUploadResponse> uploadFile(Context context, File file, String mediaType, UUID id) {

        //Get Client
        Retrofit retrofit = serviceProvider.getRetrofit();
        if(retrofit == null) throw new RuntimeException("HttpService not ready!");
        MediaRestService service = retrofit.create(MediaRestService.class);

        //Build Request
        //File file = FileUtils.getFile(context, fileUri);

        ProgressRequestBody progressRequestBody =
                new ProgressRequestBody(file, MediaType.parse(mediaType), 1); //HttpLoggingInterceptor workaround

//        MultipartBody.Part body =
//                MultipartBody.Part.createFormData("picture", file.getName(), progressRequestBody);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.parse("multipart/form-data"))
                .addFormDataPart("file", file.getName(), progressRequestBody);

        if (id != null) {
            builder.addFormDataPart("id", id.toString());
        }

        MultipartBody body = builder.build();

        progressRequestBody.getProgressSubject()
                .subscribeOn(schedulersFacade.io())
                .subscribe(percentage ->
                        Timber.tag(TAG).i("PROGRESS %i", percentage));

        //Upload
        return service.uploadMedia(body)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui());
//                .subscribe(
//                        response -> {
//                            Toast.makeText(context,"Upload SUCCESS!!", Toast.LENGTH_LONG).show();
//                        },
//                        throwable -> {
//                            Timber.tag(TAG).e(throwable);
//                            //if(postSub != null) postSub.dispose();
//                        });
    }

//    public void downloadFile(Context context, UUID id){
//
//        //Get Client
//        MediaRestService service =
//                serviceProvider.getRetrofit().create(MediaRestService.class);
//
//
//        ResponseBody body = service.getMedia(id)
//                .subscribeOn(schedulersFacade.io())
//                .observeOn(schedulersFacade.ui())
//                .subscribe(
//                        response -> {
//                            Bitmap bitmap = BitmapFactory.decodeStream(response.byteStream());
//                            bitmap.
//
//                        }
//
//
//                )
//

//                        //
//                        response -> {
//                        },
//                        throwable -> {
//                            Timber.tag(TAG).e(throwable);
//                            //if(postSub != null) postSub.dispose();
//                        }/*,
//                        () -> Toast.makeText(context,"Upload SUCCESS!!", Toast.LENGTH_LONG).show()*/
//                );


//
//        Bitmpa bm = BitmapFactory.decodeStream(response.body().byteStream());
//        ivCaptcha.setImageBitmap(bm);
//
//                .execute().body();
//        byte[] bytes = body.bytes();
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//        bitmap
//
//
//    }
//
//    private void saveImage1(Bitmap imageToSave, String fileName) {
//        // get the path to sdcard
//        File sdcard = Environment.getExternalStorageDirectory();
//        // to this path add a new directory path
//        File dir = new File(sdcard.getAbsolutePath() + "/FOLDER_NAME/");
//        // create this directory if not already created
//        dir.mkdir();
//        // create the file in which we will write the contents
//        File file = new File(dir, fileName);
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            out.flush();
//            out.close();
//            counter++;
//            // if (counter < feedList.size()) {
//            //downloadImage(counter);
//            //} else {
//            setImage();
//            //}
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sageImage(Bitmap bitmap, String fileName, String contentType){
//
//        FileUtils.
//
//    }

    public Single<File> downloadImage(UUID id) {
        try {
            File file = generateDownloadImageFile(id, ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Retrofit retrofit = serviceProvider.getRetrofit();
        if(retrofit == null) throw new RuntimeException("HttpService not ready!");
        MediaRestService service = retrofit.create(MediaRestService.class);

        return service.getMedia(id)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.newThread())
                .map(result -> {
                    if (!result.isError()) {
                        return result.response();
                    } else {
                        Timber.tag(TAG).e(result.error());
                        throw new IOException(result.error());
                    }
                })
                .map(response -> {
                    Headers headers = response.headers();
                    String type = headers.get("Content-Type");
//                    String disposition = headers.get("Content-Disposition");
//                    String filename = null;
//                    if(disposition != null) {
//                        int start = disposition.indexOf("filename=");
//                        int end = disposition.indexOf(";", start + "filename=".length());
//                        if (end == -1) end = disposition.length() - 1;
//                        filename = disposition
//                                .substring(start + "filename=".length() + 1, end)
//                                .replaceAll("\"", "");
//                    }

                    String ext = MimeTypeUtils.getExtension(type);
                    File file = generateDownloadImageFile(id, ext);

                    boolean result = writeResponseBody(response.body(), file);
                    if (result) {
                        return file;
                    }

                    Timber.tag(TAG).e("Error while reading media from server!");
                    throw new IOException("Could not read Media from Server!");

//                    if(result) {
//                        Bitmap bitmap = BitmapFactory.decodeFile(file);
//                    }
                });
    }
}
