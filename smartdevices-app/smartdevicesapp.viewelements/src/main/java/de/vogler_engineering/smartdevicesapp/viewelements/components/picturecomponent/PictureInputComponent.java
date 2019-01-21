/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.picturecomponent;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.component.PictureData;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PictureInputComponent extends BaseComponent<PictureData> implements Injectable {

    private static final String TAG = "PictureInputComponent";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FrameLayout mContainer;
    private LinearLayout mLinearLayout;
    private TextView mLabel;
    private ImageButton mAddButton;
    private ImageButton mRemoveButton;
    private RecyclerView mRecyclerView;
    private PictureListAdapter mAdapter;
    private ImageView mPlaceholderImage;
    private ImageButton mTakeImageButton;

    @Inject
    AppManager appManager;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    ResourceRepository resourceRepository;

    private PictureComponentData componentData;

    public PictureInputComponent() {
        super(ComponentType.PictureInput);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());
        mLinearLayout = (LinearLayout) inflater.inflate(R.layout.component_picture_input,
                mContainer, false);

        mLabel = mLinearLayout.findViewById(R.id.component_picture_input_name);
        mAddButton = mLinearLayout.findViewById(R.id.component_picture_input_add_image_button);
        mRemoveButton = mLinearLayout.findViewById(R.id.component_picture_input_remove_image_button);
        mTakeImageButton = mLinearLayout.findViewById(R.id.component_picture_input_take_image_button);
        mPlaceholderImage = mLinearLayout.findViewById(R.id.component_picture_input_placeholder_image);

        mRecyclerView = mLinearLayout.findViewById(R.id.component_picture_input_image_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PictureListAdapter(schedulersFacade);
        mRecyclerView.setAdapter(mAdapter);
        compositeDisposable.add(mAdapter);

        mContainer.addView(mLinearLayout);
        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData<PictureData> componentData, LifecycleOwner lifecycleOwner) {
        PictureComponentData data = ComponentData.getComponentData(componentData, PictureComponentData.class);
        this.componentData = data;
        mLabel.setText(element.getName());

        mAdapter.setItems(data.getItems());

        Boolean b = data.getDeleteActive().getValue();
        mRemoveButton.setEnabled(b == null ? false : b);
        data.getDeleteActive().observeForever(
                o -> mRemoveButton.setEnabled(o == null ? false : o));

        mTakeImageButton.setOnClickListener(l -> data.onTakePictureClicked());
        mAddButton.setOnClickListener(l -> data.onAddPictureClicked());
        mRemoveButton.setOnClickListener(l -> data.onRemovePictureClicked());


        data.getListEmptyObservable().observeForever(
                listEmpty -> {
                    if (listEmpty == null || !listEmpty) {
                        mPlaceholderImage.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    } else {
                        mPlaceholderImage.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
        );


//        mRecyclerView.setSelection(val == null ? 0 : val);
//        mRecyclerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                data.setValue(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                Integer val = data.getValue();
//                mRecyclerView.setSelection(val == null ? 0 : val);
//            }
//        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PictureComponentData.REQUEST_TAKE_PHOTO){
            if(resultCode == RESULT_OK){
                componentData.addPicture();
            }else if(resultCode == RESULT_CANCELED){
                Timber.tag(TAG).i("Take photo intent canceled!");
                componentData.takePictureCanceled();
            }
        }
    }

    @Override
    public void dispose() {
        compositeDisposable.dispose();
        mAdapter = null;
        mLabel = null;
        mAddButton = null;
        mRemoveButton = null;
        mRecyclerView = null;
        mLinearLayout = null;
        mContainer = null;
        mPlaceholderImage = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }

}
