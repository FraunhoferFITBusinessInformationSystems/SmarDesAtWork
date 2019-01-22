/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 23.03.2018.
 */

public class TextViewComponent extends BaseComponent<String> {

    private LinearLayout mLayout;
    private TextView mLabel;
    private TextView mTextView;

    public TextViewComponent() {
        super(ComponentType.TextView);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics met = context.getResources().getDisplayMetrics();
        int dp;

        mLayout = new LinearLayout(context);
        mLayout.setLayoutParams(getDefaultLayoutParams());
        mLayout.setOrientation(LinearLayout.VERTICAL);

        mLabel = new TextView(context);
        mLabel.setLayoutParams(getDefaultLayoutParams());
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mLabel.setTextColor(context.getResources().getColor(R.color.textDarkSecondary, null));
        mLayout.addView(mLabel);

        mTextView = new TextView(context);
        mTextView.setId(View.generateViewId());
        LinearLayout.LayoutParams tvParams = getDefaultLayoutParams();
        tvParams.setMargins(0, getFromDp(met, 4), 0, getFromDp(met,8));
        mTextView.setLayoutParams(tvParams);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTextView.setTextColor(context.getResources().getColor(R.color.textDarkPrimary, null));
        mTextView.setMinHeight(getFromDp(met,8));
        mLayout.addView(mTextView);

        return mLayout;
    }

    @Override
    public void bindView(Context context, ComponentData<String> componentData, LifecycleOwner lifecycleOwner) {
        mLabel.setText(element.getName());
        final TextViewComponentData data = ComponentData.getComponentData(componentData, TextViewComponentData.class);

        mTextView.setText(data.getValue());
        if(this.lifecycleOwner != null && this.lifecycleOwner.get() != null){
            data.valueLiveData().observe(this.lifecycleOwner.get(), this::updateData);
        }else{
            //TODO REMOVE!!! this will produce memory leaks!
            data.valueLiveData().observeForever(this::updateData);
        }
    }

    @Override
    protected void updateData(String s) {
        mTextView.setText(s);
    }

    @Override
    public void dispose() {
        mTextView = null;
        mLabel = null;
        mLayout = null;
    }

    @Override
    public View getView() {
        return mLayout;
    }
}