/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import timber.log.Timber;

/**
 * Created by vh on 23.03.2018.
 */

public class BarcodeInputComponent extends BaseComponent<String> {

    private static final String TAG = "BarcodeInputComponent";

    private FrameLayout mContainer;

    private EditText mEditText;
    private TextWatcher mTextWatcher;
    private Button mScanButton;
    private TextView mTextView;

    private IntentResult mResult = null;

    public BarcodeInputComponent() {
        super(ComponentType.BarcodeInput);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int dp;

        mContainer = new FrameLayout(context);
        mContainer.setId(View.generateViewId());
        mContainer.setLayoutParams(getDefaultLayoutParams());

        View layout = inflater.inflate(R.layout.component_barcode_input,
                mContainer, false);
        mContainer.addView(layout);

        mTextView = layout.findViewById(R.id.comp_barcode_input_name);
        mEditText = layout.findViewById(R.id.comp_barcode_input_text);
        mScanButton = layout.findViewById(R.id.comp_barcode_input_button);

        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData<String> componentData, LifecycleOwner lifecycleOwner) {
        mTextView.setText(element.getName());
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

        mScanButton.setOnClickListener((v) -> {
            IntentIntegrator integrator = createIntentIntegrator(context);
            if(integrator == null){
                Timber.tag(TAG).e("Could not Initialize IntentIntegrator for Barcode Scanner. Insufficient Context supplied!");
                return;
            }

            integrator.setOrientationLocked(false);
            integrator.setPrompt(context.getResources().getString(R.string.component_barcode_input_scan_text));
            integrator.initiateScan();
        });
    }

    private IntentIntegrator createIntentIntegrator(Context context){
        if(Fragment.class.isInstance(context)){
            return IntentIntegrator.forSupportFragment(Fragment.class.cast(context));
        }else if(Activity.class.isInstance(context)){
            return new IntentIntegrator(Activity.class.cast(context));
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Timber.tag(TAG).i("Scanning canceled!");
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Timber.tag(TAG).i("Scan result: %s", result);
                //Toast.makeText(this, "Scanned!", Toast.LENGTH_LONG).show();
                if(mEditText != null){
                    mEditText.setText(result.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    @Override
    public void dispose() {
//        mEditText.removeTextChangedListener(mTextWatcher);
//        mEditText.setOnClickListener(null);
        mTextWatcher = null;
//        mEditText = null;
        mScanButton = null;
        mContainer = null;
        mResult = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}