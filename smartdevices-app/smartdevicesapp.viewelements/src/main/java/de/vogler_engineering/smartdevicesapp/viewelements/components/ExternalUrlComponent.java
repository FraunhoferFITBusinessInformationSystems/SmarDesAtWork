/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 23.03.2018.
 */

public class ExternalUrlComponent extends BaseComponent<String> {

    private FrameLayout mContainer;
    private TextView mLabel;
    private TextView mTextView;
    private View mClickView;

    public ExternalUrlComponent() {
        super(ComponentType.ExternalUrl);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics met = context.getResources().getDisplayMetrics();
        int dp;

        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());
        View view = inflater.inflate(R.layout.component_external_url,
                mContainer, false);

        mLabel = view.findViewById(R.id.comp_external_url_name);
        mTextView = view.findViewById(R.id.comp_external_url_text);
        mClickView = view.findViewById(R.id.comp_external_url_click);

        mContainer.addView(view);
        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData<String> componentData, LifecycleOwner lifecycleOwner) {
        final ExternalUrlComponentData data = ComponentData.getComponentData(componentData, ExternalUrlComponentData.class);

        mLabel.setText(element.getName());
        String uriStr = data.getValue();
        if(uriStr == null) {
            uriStr = getFromAdditionalProperties("uri", String.class);
        }
        if(uriStr == null){
            uriStr = getFromAdditionalProperties("url", String.class);
        }
        updateData(uriStr);
        if(this.lifecycleOwner != null && this.lifecycleOwner.get() != null){
            data.valueLiveData().observe(this.lifecycleOwner.get(), this::updateData);
        }else{
            data.valueLiveData().observeForever(this::updateData);
        }

        mClickView.setOnClickListener((l) -> data.onClick());
    }

    @Override
    protected void updateData(String s) {
        mTextView.setText(s);
    }

    @Override
    public void dispose() {
        mTextView = null;
        mLabel = null;
        mClickView = null;
        mContainer = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}