/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.common.util.StringUtil;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiAction;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.util.ResourceUtils;
import timber.log.Timber;

/**
 * Created by vh on 23.03.2018.
 */

public class GenericActionComponent extends BaseComponent<Void> {

    protected static final String TAG = "GenericActionComponent";

    protected FrameLayout mContainer;
    protected RelativeLayout mLayout;

    protected TextView mTextView;
    protected ImageView mImageView;

    public GenericActionComponent() {
        super(ComponentType.GenericAction);
    }
    public GenericActionComponent(ComponentType cType) {
        super(cType);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        mContainer = new FrameLayout(context);
        mContainer.setId(View.generateViewId());
        mContainer.setLayoutParams(getDefaultLayoutParams());

        mLayout = (RelativeLayout)inflater.inflate(R.layout.component_generic_action,
                mContainer, false);
        mContainer.addView(mLayout);

        mTextView = mLayout.findViewById(R.id.generic_action_text);
        mImageView = mLayout.findViewById(R.id.generic_action_image);

        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData<Void> componentData, LifecycleOwner lifecycleOwner) {
        final GenericActionComponentData data = ComponentData.getComponentData(componentData, GenericActionComponentData.class);
        final UiAction exactElement = UiAction.fromComponent(element);

        mTextView.setText(element.getName());

        String image = getFromAdditionalProperties("image", String.class);
        if(image != null){
            int resId = ResourceUtils.getIconResourceByKey(image);
            if(resId == ResourceUtils.INVALID_RESOURCE){
                Timber.tag(TAG).e("Could not find specified drawable: %s", image);
            }else {
                mImageView.setImageDrawable(context.getResources().getDrawable(resId, null));
            }
        }

        if(exactElement != null && StringUtil.isNotNullOrEmpty(exactElement.getJobKey())){
            String jobKey = exactElement.getJobKey();
            if(data == null){
                Log.d(TAG,"Can not set click listener -> ComponentData NULL !");
            }
            mLayout.setOnClickListener((l) -> data.onStartJob(jobKey));
        }
    }

    @Override
    public void dispose() {
        mImageView = null;
        mTextView = null;
        mLayout = null;
        mContainer = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}
