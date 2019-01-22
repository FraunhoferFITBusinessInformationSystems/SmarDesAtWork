/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

/**
 * Created by vh on 23.03.2018.
 */

public class TextInputComponent extends BaseComponent<String> {

    private TextInputEditText mEditText;
    private TextInputLayout mTextInputLayout;
    private TextWatcher mTextWatcher;



    public TextInputComponent() {
        super(ComponentType.TextInput);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int dp;

        mEditText = new TextInputEditText(context);
        mEditText.setId(View.generateViewId());
        mEditText.setLayoutParams(getDefaultLayoutParams());
        mEditText.setMaxLines(1);
        mEditText.setSingleLine(true);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

//        // Set color of the hint text inside the EditText field
//        editText.setHintTextColor(getResources().getColor(android.R.color.white));
//        // Set the font size of the text that the user will enter
//        editText.setTextSize(16);
//        // Set the color of the text inside the EditText field
//        editText.setTextColor(getResources().getColor(android.R.color.white));

        mTextInputLayout = new TextInputLayout(context);
        mTextInputLayout.setId(View.generateViewId());

        LinearLayout.LayoutParams layoutParams = getDefaultLayoutParams();
        layoutParams.setMargins(0, getFromDp(metrics, 8), 0, 0);
        mTextInputLayout.setLayoutParams(layoutParams);

        dp = getFromDp(metrics, -4);
        mTextInputLayout.setPadding(dp, 0, dp, 0);

        mTextInputLayout.addView(mEditText);
        return mTextInputLayout;
    }

    @Override
    public void bindView(Context context, ComponentData<String> componentData, LifecycleOwner lifecycleOwner) {
        mEditText.setHint(element.getName());
        final TextInputComponentData data = ComponentData.getComponentData(componentData, TextInputComponentData.class);

        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!mEditText.getText().toString().equals(data.getValue())){
                    data.setValue(mEditText.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
        mEditText.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void dispose() {
        mEditText.removeTextChangedListener(mTextWatcher);
        mTextWatcher = null;
        mEditText = null;
        mTextInputLayout = null;
    }

    @Override
    public View getView() {
        return mTextInputLayout;
    }
}