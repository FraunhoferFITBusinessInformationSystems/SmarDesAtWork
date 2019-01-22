/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.value.GenericValueData;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class ValueMonitorComponentSingle extends BaseComponent<GenericValueData> {

    public ValueMonitorComponentSingle() {
        super(ComponentType.ValueMonitorSingle);
    }

    protected FrameLayout mContainer;
    protected GridLayout mGridLayout;

    protected TextView mDisplayNameTextView;
    protected TextView mNameTextView;
    protected TextView mValueActualTextView;
    protected TextView mTextTextView;

    protected String mPrefix = "";
    protected String mPostfix = "";

    private final static int COLOR_NORMAL = Color.rgb(0xFF, 0xFF, 0xFF);                 //#FFFFFF
    private final static int COLOR_WARNING = Color.argb(0xFF, 0xFF, 0xFF, 0x00); //#88FFFF00
    private final static int COLOR_ERROR = Color.argb(0xFF, 0xD5, 0x00, 0x00);  //#88FF0000

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());

        mGridLayout = (GridLayout) inflater.inflate(R.layout.component_value_monitor_single,
                mContainer, false);

        mDisplayNameTextView = mGridLayout.findViewById(R.id.comp_value_monitor_display_name);
        mNameTextView = mGridLayout.findViewById(R.id.comp_value_monitor_name);
        mValueActualTextView = mGridLayout.findViewById(R.id.comp_value_monitor_value_actual);
        mTextTextView = mGridLayout.findViewById(R.id.comp_value_monitor_text);

        mContainer.addView(mGridLayout);
        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData componentData, LifecycleOwner lifecycleOwner) {
        final ValueMonitorComponentData data = (ValueMonitorComponentData) componentData;

        data.enablePolling(lifecycleOwner);

        update(data);

        data.valueLiveData().observeForever(x -> update(data));
    }

    private void update(ValueMonitorComponentData data){
        mNameTextView.setText(data.getId());
        mDisplayNameTextView.setText(data.getName());
        mValueActualTextView.setText(data.getActualValue());

        if(data.hasInfoText()){
            mTextTextView.setVisibility(View.VISIBLE);
            mTextTextView.setText(data.getInfoText());
        }else{
            mTextTextView.setVisibility(View.GONE);
        }

        int sev = data.getSeverity();
        int col = COLOR_NORMAL;
        if (sev != ValueMonitorComponentData.SEVERITY_NORMAL) {
            if (sev == ValueMonitorComponentData.SEVERITY_WARNING) {
                col = COLOR_WARNING;
            } else if (sev == ValueMonitorComponentData.SEVERITY_ERROR) {
                col = COLOR_ERROR;
            }
        }
        mContainer.setBackgroundColor(col);
    }

    @Override
    public void dispose() {
        mDisplayNameTextView = null;
        mNameTextView = null;
        mValueActualTextView = null;
        mTextTextView = null;
        mContainer = null;
        mGridLayout = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}