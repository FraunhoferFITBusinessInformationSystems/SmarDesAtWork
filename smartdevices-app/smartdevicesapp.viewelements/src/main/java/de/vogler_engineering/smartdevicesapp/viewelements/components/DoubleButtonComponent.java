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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 23.03.2018.
 */

public class DoubleButtonComponent extends BaseComponent<Void> {

    protected FrameLayout mContainer;
    protected RelativeLayout mWrappingLayout;
    protected Button mPrimaryButton;
    protected Button mSecondaryButton;

    public DoubleButtonComponent() {
        super(ComponentType.DoubleButton);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        mContainer = new FrameLayout(context);
        mContainer.setId(View.generateViewId());
        mContainer.setLayoutParams(getDefaultLayoutParams());

        mWrappingLayout = (RelativeLayout)inflater.inflate(R.layout.component_two_button_view,
                mContainer, false);

        mPrimaryButton = mWrappingLayout.findViewById(R.id.two_button_view_primary);
        mSecondaryButton = mWrappingLayout.findViewById(R.id.two_button_view_secondary);

        mContainer.addView(mWrappingLayout);
        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData<Void> componentData, LifecycleOwner lifecycleOwner) {
        final DoubleButtonComponentData data = ComponentData.getComponentData(componentData, DoubleButtonComponentData.class);

        mPrimaryButton.setText(element.getName());

        final String o = getFromAdditionalProperties("TextSecondary", String.class);
        if(o != null && !o.isEmpty()){
            mSecondaryButton.setText(o);
        }

        mPrimaryButton.setOnClickListener((l) -> data.onClickPrimary());
        mSecondaryButton.setOnClickListener((l) -> data.onClickSecondary());
    }

    @Override
    public void dispose() {
        mPrimaryButton.setOnClickListener(null);
        mSecondaryButton.setOnClickListener(null);
        mWrappingLayout = null;
        mContainer = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}
