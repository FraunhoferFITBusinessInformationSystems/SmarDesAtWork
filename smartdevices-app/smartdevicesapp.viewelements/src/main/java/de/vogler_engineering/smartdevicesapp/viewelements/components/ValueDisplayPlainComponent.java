/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.value.ValueData;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class ValueDisplayPlainComponent extends BaseComponent<ValueData> {

    public ValueDisplayPlainComponent() {
        super(ComponentType.ValueDisplayPlain);
    }

    protected FrameLayout mContainer;
    protected GridLayout mGridLayout;

    protected TextView mDisplayNameTextView;
    protected TextView mNameTextView;
    protected TextView mMinNameTextView;
    protected TextView mMaxNameTextView;
    protected TextView mValueNameTextView;

    protected String mPrefix = "";
    protected String mPostfix = "";

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());
        mGridLayout = (GridLayout) inflater.inflate(R.layout.component_value_display_plain,
                mContainer, false);

        mDisplayNameTextView = mGridLayout.findViewById(R.id.comp_value_display_plain_display_name);
        mNameTextView = mGridLayout.findViewById(R.id.comp_value_display_plain_name);
        mMinNameTextView = mGridLayout.findViewById(R.id.comp_value_display_plain_min);
        mMaxNameTextView = mGridLayout.findViewById(R.id.comp_value_display_plain_max);
        mValueNameTextView = mGridLayout.findViewById(R.id.comp_value_display_plain_value);

        mContainer.addView(mGridLayout);
        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData componentData, LifecycleOwner lifecycleOwner) {
        final ValueDisplayPlainComponentData data = (ValueDisplayPlainComponentData) componentData;

        mNameTextView.setText(element.getId());
        mDisplayNameTextView.setText(element.getName());

        final Map<String, Object> props = element.getAdditionalProperties();

        if (props.containsKey("minValue")) {
            mMinNameTextView.setText(String.format("Min: %s", props.get("minValue")));
        } else mMinNameTextView.setText("");

        if (props.containsKey("maxValue")) {
            mMaxNameTextView.setText(String.format("Max: %s", props.get("maxValue")));
        } else mMaxNameTextView.setText("");

        if (props.containsKey("unit")) {
            mPostfix = String.valueOf(props.get("unit"));
        }

        String initial = "0";
        if (props.containsKey("initValue")) {
            initial = String.valueOf(props.get("initValue"));
        }
        mValueNameTextView.setText(String.format("%s%s%s", mPrefix, initial, mPostfix));

        data.valueLiveData().observe((LifecycleOwner) context, (valueData) -> {
            String value;
            if (valueData == null) {
                value = "---";
            } else {
                value = String.valueOf(valueData.getValue());
            }
            mValueNameTextView.setText(String.format("%s%s%s", mPrefix, value, mPostfix));
        });
    }

    @Override
    public void dispose() {
        mDisplayNameTextView = null;
        mNameTextView = null;
        mMinNameTextView = null;
        mMaxNameTextView = null;
        mValueNameTextView = null;
        mContainer = null;
        mGridLayout = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}