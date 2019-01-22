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
import android.widget.LinearLayout;
import android.widget.Switch;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 27.03.2018.
 */

public class SwitchComponent extends BaseComponent<Boolean> {

    private Switch mSwitch;

    public SwitchComponent() {
        super(ComponentType.Switch);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        mSwitch = new Switch(context);
        mSwitch.setId(View.generateViewId());
        LinearLayout.LayoutParams spinnerLayout = getDefaultLayoutParams();
        int a = getFromDp(metrics, 16);
        spinnerLayout.setMargins(0, a, 0, a);
        mSwitch.setLayoutParams(spinnerLayout);

        return mSwitch;
    }

    @Override
    public void bindView(Context context, ComponentData<Boolean> componentData, LifecycleOwner lifecycleOwner) {
        mSwitch.setText(element.getName());
        final SwitchComponentData data = (SwitchComponentData) componentData;

        Boolean b = componentData.getValue();
        mSwitch.setChecked(b==null?false:b);
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            data.setValue(mSwitch.isChecked());
        });
    }

    @Override
    public void dispose() {
        mSwitch = null;
    }

    @Override
    public View getView() {
        return mSwitch;
    }
}
