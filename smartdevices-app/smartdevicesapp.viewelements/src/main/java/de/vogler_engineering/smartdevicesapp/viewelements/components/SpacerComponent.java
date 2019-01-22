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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 23.03.2018.
 */

public class SpacerComponent extends BaseComponent<Void> {

    protected LinearLayout mLayout;
    protected View mSpacer;
    protected TextView mLabel;

    public SpacerComponent() {
        super(ComponentType.Spacer);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics met = context.getResources().getDisplayMetrics();

        mLayout = new LinearLayout(context);
        mLayout.setLayoutParams(getDefaultLayoutParams());
        mLayout.setOrientation(LinearLayout.VERTICAL);

        mSpacer = new View(context);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getFromDp(met,2));
        spacerParams.setMargins(0, 0, 0, getFromDp(met, 4));
        mSpacer.setLayoutParams(spacerParams);
        mSpacer.setBackgroundColor(context.getResources().getColor(R.color.textDarkSecondary, null));
        mLayout.addView(mSpacer);

        mLabel = new TextView(context);
        LinearLayout.LayoutParams labelParams = getDefaultLayoutParams();
        labelParams.setMargins(0, 0, 0, getFromDp(met, 4));
        mLabel.setLayoutParams(labelParams);
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mLabel.setTextColor(context.getResources().getColor(R.color.textDarkSecondary, null));
        mLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mLabel.setText(element.getName());
        mLayout.addView(mLabel);

        return mLayout;
    }

    @Override
    public void bindView(Context context, ComponentData<Void> componentData, LifecycleOwner lifecycleOwner) {
//        final ButtonComponentData data = ComponentData.getComponentData(componentData, ButtonComponentData.class);
        mLabel.setText(element.getName());
    }

    @Override
    public void dispose() {
//        mButton.setOnClickListener(null);
        mLabel = null;
        mSpacer = null;
        mLayout = null;
    }

    @Override
    public View getView() {
        return mLayout;
    }
}