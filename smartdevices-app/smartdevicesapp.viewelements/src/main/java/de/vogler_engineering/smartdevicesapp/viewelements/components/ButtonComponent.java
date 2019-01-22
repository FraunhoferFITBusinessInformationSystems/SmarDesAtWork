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
import android.widget.LinearLayout;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 23.03.2018.
 */

public class ButtonComponent extends BaseComponent<Void> {

    protected Button mButton;

    public ButtonComponent() {
        super(ComponentType.Button);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        mButton = new Button(context);
        mButton.setId(View.generateViewId());

        String string = context.getResources().getString(R.string.conf_device_type);
        if(string.equals("watch")) {
            LinearLayout.LayoutParams layoutParams = getDefaultLayoutParams();

            int dp = getFromDp(metrics, 5);
            layoutParams.setMargins(4*dp, 2*dp, 5*dp, 2*dp);
            mButton.setLayoutParams(layoutParams);

            mButton.setMinHeight(getFromDp(metrics, 45));
        }else {
            mButton.setLayoutParams(getDefaultLayoutParams());
            mButton.setMinHeight(getFromDp(metrics, 65));
        }

        return mButton;
    }

    @Override
    public void bindView(Context context, ComponentData<Void> componentData, LifecycleOwner lifecycleOwner) {
        final ButtonComponentData data = ComponentData.getComponentData(componentData, ButtonComponentData.class);

        mButton.setText(element.getName());
        mButton.setOnClickListener((l) -> data.onClick());

        final Boolean o = getFromAdditionalProperties("Primary", Boolean.class);
        if(o != null && o){
            mButton.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
        }
    }

    @Override
    public void dispose() {
        mButton.setOnClickListener(null);
        mButton = null;
    }

    @Override
    public View getView() {
        return mButton;
    }
}
