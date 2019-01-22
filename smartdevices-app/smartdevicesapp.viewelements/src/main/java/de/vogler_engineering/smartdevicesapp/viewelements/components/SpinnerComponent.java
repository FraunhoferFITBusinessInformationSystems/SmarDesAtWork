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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 27.03.2018.
 */

public class SpinnerComponent extends BaseComponent<Integer> {

    private LinearLayout mLayout;
    private TextView mLabel;
    private Spinner mSpinner;
    private View mPlaceholder;

    public SpinnerComponent() {
        super(ComponentType.Spinner);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        mLayout = new LinearLayout(context);
        mLayout.setLayoutParams(getDefaultLayoutParams());
        mLayout.setOrientation(LinearLayout.VERTICAL);

        mLabel = new TextView(context);
        mLabel.setLayoutParams(getDefaultLayoutParams());
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mLabel.setTextColor(context.getResources().getColor(R.color.textDarkSecondary, null));
        mLayout.addView(mLabel);

        mSpinner = new Spinner(context);
        mSpinner.setId(View.generateViewId());
        LinearLayout.LayoutParams spinnerLayout = getDefaultLayoutParams();
        int a = getFromDp(metrics, 8);
        int b = getFromDp(metrics, 4);
        spinnerLayout.setMargins(-a, b, -a, a);
        mSpinner.setLayoutParams(spinnerLayout);
        mLayout.addView(mSpinner);

        mPlaceholder = new View(context);
        mPlaceholder.setBackgroundColor(context.getResources().getColor(R.color.textDarkPrimary, null));
        LinearLayout.LayoutParams placeholderLayout =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getFromDp(metrics, 1));
        placeholderLayout.setMargins(0, 0, 0, getFromDp(metrics, 4));
        mPlaceholder.setLayoutParams(placeholderLayout);
        mLayout.addView(mPlaceholder);

        return mLayout;
    }

    @Override
    public void bindView(Context context, ComponentData<Integer> componentData, LifecycleOwner lifecycleOwner) {
        mLabel.setText(element.getName());

        SpinnerComponentData data = (SpinnerComponentData) componentData;

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, data.getLabels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        Integer val = data.getValue();
        mSpinner.setSelection(val == null ? 0 : val);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setValue(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Integer val = data.getValue();
                mSpinner.setSelection(val == null ? 0 : val);
            }
        });
    }

    @Override
    public void dispose() {
        mLabel = null;
        mSpinner = null;
        mPlaceholder = null;
        mLayout = null;
    }

    @Override
    public View getView() {
        return mLayout;
    }
}
