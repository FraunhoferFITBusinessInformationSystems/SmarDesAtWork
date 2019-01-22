/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiConfig;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * Created by vh on 26.03.2018.
 */

public class ResourceRepository extends AbstractRepository{

    private static final String TAG = "ResourceRepository";
    
    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;
    private final RestServiceProvider restServiceProvider;

    private final HashMap<String, MutableLiveData<Object>> resourceCache = new HashMap<>();
    private final HashMap<String, Boolean> requestActive = new HashMap<>();

    private final MutableLiveData<UiConfig> uiConfig = new MutableLiveData<>();
    private boolean reconfigure = false;

    private OkHttpClient client = null;
    private Picasso picasso;

    public final static String SMARDES_URI_SCHEME = "smardes";
    public final static String SMARDES_URI_RESOURCE_AUTHORITY = "res";

    public ResourceRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        super(appManager, schedulersFacade);
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
        this.restServiceProvider = restServiceProvider;
    }

    protected void createPicasso(Context context){
        if(picasso != null){
            picasso.shutdown();
            //TODO cancel all requests?
        }

        //Create new Instance
        client = restServiceProvider.getHttpClient();
        if(client == null) throw new RuntimeException("HttpService not ready!");
        OkHttp3Downloader downloader = new OkHttp3Downloader(client);
//        Picasso.RequestTransformer transformer = new Picasso.RequestTransformer() {
//            @Override
//            public Request transformRequest(Request request) {
//                Request ret = request;
//                if(request.uri.isRelative()){
//
//                }else if(SMARDES_URI_SCHEME.equalsIgnoreCase(request.uri.getScheme()) &&
//                        SMARDES_URI_RESOURCE_AUTHORITY.equalsIgnoreCase(request.uri.getAuthority())){
//                    Uri uri = Uri.parse("/" + request.uri.getPath());
//                    ret = new Request.Builder().set
//                }
//            }
//        }

        picasso = new Picasso.Builder(context)
//                .requestTransformer(transformer)
                .downloader(downloader)
                .loggingEnabled(true)
                .memoryCache(new LruCache(context))
                .listener((picasso, uri, exception)
                        -> Timber.tag(TAG).e(exception, "Exception while loading Image with Picasso from [%s]", uri.toString()))
                .build();
    }

    protected Picasso getPicasso(){
        OkHttpClient newClient = restServiceProvider.getHttpClient();
        if(newClient != client){
            createPicasso(appManager.getContext());
        }
        return picasso;
    }

    public RequestCreator loadImage(String resourceKey) throws ImageLoadingException {
        try{
            URI uri = URI.create(resourceKey);

            String path = "/api/" + uri.getHost() + uri.getPath();
            Uri requestUri = Uri.parse(appManager.getBaseUrl() + path);

            return getPicasso().load(requestUri);
        }catch (Exception e){
            throw new ImageLoadingException("Unparsable URI specified!", e);
        }
    }

//    public LiveData<Object> getResource(String name){
//        if(     ( reconfigure
//                || !resourceCache.containsKey(name)
//                || resourceCache.get(name).getValue() == null )
//                && ( requestActive.get(name) == null || !requestActive.get(name) )){
//            updateResource(name);
//        }
//        return resourceCache.get(name);
//    }

//    private Single<Object> updateResource(String name) {
//        requestActive.put(name, true);
//
//        ResourceRestService service = restServiceProvider.createRestService(ResourceRestService.class);
//        //Single<Result<ResponseBody>> resource = service.getResource(name);
//
//        return service.getResource(name)
//                .subscribeOn(schedulersFacade.io())
//                .observeOn(schedulersFacade.newThread())
//                .map(result -> {
//                    if (!result.isError()) {
//                        return result.response();
//                    } else {
//                        Timber.tag(TAG).e(result.error());
//                        throw new IOException(result.error());
//                    }
//                })
//                .map(response -> {
//                    Headers headers = response.headers();
//                    String type = headers.get("Content-Type");
////                    String disposition = headers.get("Content-Disposition");
////                    String filename = null;
////                    if(disposition != null) {
////                        int start = disposition.indexOf("filename=");
////                        int end = disposition.indexOf(";", start + "filename=".length());
////                        if (end == -1) end = disposition.length() - 1;
////                        filename = disposition
////                                .substring(start + "filename=".length() + 1, end)
////                                .replaceAll("\"", "");
////                    }
//
//                    String ext = MimeTypeUtils.getExtension(type);
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
//
//                    File file = generateDownloadTempFile(name);
//
//                    boolean result = writeResponseBody(response.body(), file);
//                    requestActive.put(name, false);
//                    if (result) {
//                        return file;
//                    }
//
//                    Timber.tag(TAG).e("Error while reading resource from server!");
//                    throw new IOException("Could not read resource from Server!");
//
////                    if(result) {
////                        Bitmap bitmap = BitmapFactory.decodeFile(file);
////                    }
//
//                });
//    }

//    private final static String GUI_NAME = "gui";

//    public LiveData<UiConfig> getUiConfig(){
//        if( ( reconfigure || uiConfig.getValue() == null)
//                && ( !hasActiveRequest(GUI_NAME) )){
//            updateUiConfig();
//        }
//        return uiConfig;
//    }

//    public boolean updateUiConfig(){
//        if(hasActiveRequest(GUI_NAME))
//            return false;
//        requestActive.put(GUI_NAME, true);
//        ResourceRestService service = restServiceProvider.createRestService(ResourceRestService.class);
//        service.getUiConfig()
//                .subscribeOn(schedulersFacade.io())
//                .observeOn(schedulersFacade.newThread())
//                .subscribe(
//                        value -> {
//                            this.uiConfig.postValue(value);
//                            requestActive.put(GUI_NAME, false);
//                        },
//                        (e) -> Timber.tag(TAG).e(e, "Error while getting UI config"));
//        return true;
//    }
//
//    public boolean hasActiveRequest(String resourceName){
//        if(requestActive.get(resourceName) == null) return false;
//        return requestActive.get(resourceName);
//    }

//    public void update() {
//        updateUiConfig();
//    }

    public Path getTemporaryImageFile() throws IOException {
        //TODO
//        return temporaryImageFile;
        return null;
    }

    public Uri getFileProvicerUri(Context appContext, Path mPhotoFile) {
        //TODO
        return null;
    }

    public class ImageLoadingException extends Exception {
        public ImageLoadingException() {
        }

        public ImageLoadingException(String message) {
            super(message);
        }

        public ImageLoadingException(String message, Throwable cause) {
            super(message, cause);
        }

        public ImageLoadingException(Throwable cause) {
            super(cause);
        }
    }
}
