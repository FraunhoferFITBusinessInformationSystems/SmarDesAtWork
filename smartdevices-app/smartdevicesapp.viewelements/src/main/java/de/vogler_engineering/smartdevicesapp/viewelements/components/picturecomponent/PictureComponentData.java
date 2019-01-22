/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.picturecomponent;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.component.PictureData;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import timber.log.Timber;

public class PictureComponentData extends ComponentData<PictureData> implements Injectable {

    private static final String TAG = "PictureComponentData";
    private final static String LIST_SEPARATOR = ";";
    final static int REQUEST_TAKE_PHOTO = 200;

    private final ObservableArrayList<PictureEntry> items = new ObservableArrayList<>();
    private final MutableLiveData<Boolean> deleteActive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> listEmpty = new MutableLiveData<>();
    private boolean mAddImageDebounce = false;

    @Inject
    Context appContext;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    ResourceRepository resourceRepository;
    private Path mPhotoFile;

    public PictureComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);

        deleteActive.postValue(false);
        listEmpty.postValue(true);

        items.addOnListChangedCallback(new PictureListComponentDataObservable());

    }

    public LiveData<Boolean> getDeleteActive() {
        return deleteActive;
    }

    @Override
    public void setResourceValue(String s) {
        if(s == null)
            return;
        String[] splits = s.split(LIST_SEPARATOR);
        for (String split : splits) {
            try{
                UUID id = UUID.fromString(split);
                addOrUpdatePictureEntry(new PictureEntry(id));
            }catch (IllegalArgumentException ignored){ }
        }
    }

    @Override
    public String getResourceValue() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PictureEntry e : items) {
            if(first) first = false;
            else sb.append(LIST_SEPARATOR);

            sb.append(e.getId());
        }
        return sb.toString();
    }

    public void addOrUpdatePictureEntry(PictureEntry pictureEntry){
        int oldEntryId = getPictureEntryIdByUUID(pictureEntry.getId());
        if(oldEntryId < 0){
            items.add(pictureEntry);
        }else{
            items.set(oldEntryId, pictureEntry);
        }
    }

    private int getPictureEntryIdByUUID(UUID id){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    private PictureEntry getPictureEntryByUUID(UUID id){
        int i = getPictureEntryIdByUUID(id);
        return (i < 0) ? null : items.get(i);
    }

    public LiveData<Boolean> getListEmptyObservable() {
        return listEmpty;
    }

    public ObservableArrayList<PictureEntry> getItems() {
        return items;
    }



    public void onAddPictureClicked() {
        if(mAddImageDebounce){
            Timber.tag(TAG).i("Add photo clicked too often! Ignoring this click.");
        }else{

        }

        //TODO

//        this.features

//        PictureEntry entry = viewModel.getEntries().get(position);
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(entry.getUri(getContext()), "image/jpeg");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(appContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            mPhotoFile = null;
            try {
                mPhotoFile = resourceRepository.getTemporaryImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Timber.tag(TAG).e(ex, "Error while saving picture.");
                Toast.makeText(appContext, "Error while saving picture.", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (mPhotoFile != null) {
                Uri photoURI = resourceRepository.getFileProvicerUri(appContext, mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if (appContext instanceof Activity) {
                    ((Activity) appContext).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } else {
                    Timber.tag(TAG).e("mContext should be an instanceof Activity.");
                }
            }
        }
    }

    public void onRemovePictureClicked() {
        //TODO
    }

    public void onTakePictureClicked() {
        //TODO
    }

    public void addPicture() {


        mAddImageDebounce = false;
        mPhotoFile = null;
    }

    public void takePictureCanceled() {


        mAddImageDebounce = false;
        mPhotoFile = null;
    }

    private class PictureListComponentDataObservable extends ObservableList.OnListChangedCallback<ObservableList<PictureEntry>> {

        @Override
        public void onChanged(ObservableList<PictureEntry> sender) {
            if(items.size() == 0) {
                if (listEmpty.getValue() == null || !listEmpty.getValue())
                    listEmpty.postValue(true);
            } else {
                if( listEmpty.getValue() == null || listEmpty.getValue())
                    listEmpty.postValue(false);
            }
        }

        @Override
        public void onItemRangeChanged(ObservableList<PictureEntry> sender, int positionStart, int itemCount) { }
        @Override
        public void onItemRangeInserted(ObservableList<PictureEntry> sender, int positionStart, int itemCount) { }
        @Override
        public void onItemRangeMoved(ObservableList<PictureEntry> sender, int fromPosition, int toPosition, int itemCount) { }
        @Override
        public void onItemRangeRemoved(ObservableList<PictureEntry> sender, int positionStart, int itemCount) { }
    }

}
