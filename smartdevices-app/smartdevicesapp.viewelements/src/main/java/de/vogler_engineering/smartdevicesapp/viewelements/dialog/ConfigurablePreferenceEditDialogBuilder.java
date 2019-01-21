/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.StringRes;
import androidx.core.widget.TextViewCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.job.JobStatus;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DisplayUtils;
import timber.log.Timber;

import static de.vogler_engineering.smartdevicesapp.viewelements.util.DisplayUtils.getFromDp;

public class ConfigurablePreferenceEditDialogBuilder extends AlertDialog.Builder {

    private static final String TAG = "ConfigurablePreferenceEditDialogBuilder";
    private final String mPrefTabKey;
    private final DisplayMetrics mDisplayMetrics;
    private final String mPrefMenuKey;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;
    private boolean mDialogCanceled = false;

    private LinearLayout mEntryLayout = null;

    @SuppressLint("CommitPrefEdits")
    public ConfigurablePreferenceEditDialogBuilder(Context context, String prefTabKey, String prefMenuKey) {
        super(context);
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefEditor = mPref.edit();
        mPrefTabKey = prefTabKey;
        mPrefMenuKey = prefMenuKey;
        mDisplayMetrics = DisplayUtils.getMetrics(getContext());
    }

    protected void setupDialog(String title, DialogButtonOptions buttonOptions) {
        this.setTitle(title);

        View dialogView = LayoutInflater.from(
                getContext()).inflate(R.layout.dialog_configurable_preference_edit, null);
        setView(dialogView);
        mEntryLayout = dialogView.findViewById(R.id.dialog_configurable_preference_edit_list);


        //Setup Button Options
        if(buttonOptions != DialogButtonOptions.Ok && buttonOptions != DialogButtonOptions.OkCancel) {
            throw new IllegalArgumentException("Unsupportet Dialog Button Options");
        }else {
            this.setPositiveButton(R.string.dialog_accept, (dialog, id) ->  mPrefEditor.apply());

            if (buttonOptions == DialogButtonOptions.OkCancel) {
                this.setNegativeButton(R.string.dialog_dismiss, (dialog, id) -> {
                    Timber.tag(TAG).d("Dialog canceled.");
                    mDialogCanceled = true;
                });
            }
        }
    }

    public boolean isDialogCanceled() {
        return mDialogCanceled;
    }

    public DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    protected void addEditOption(View layout){
        mEntryLayout.addView(layout);
    }

    protected String getPreferenceKey(String variableKey){
        return getPreferenceKey(variableKey, null);
    }

    protected String getPreferenceKey(String variableKey, String postfix){
        String key = PreferenceUtils.getMenuPrefVarName(mPrefTabKey, mPrefMenuKey, variableKey, postfix);
        Timber.tag(TAG).d("Requested Preference Key: %s", key);
        return key;
    }

    protected FrameLayout createViewWrapper(){
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return frameLayout;
    }

    protected FrameLayout createIndentedViewWrapper(View child){
        FrameLayout viewWrapper = createIndentedViewWrapper();
        if(child != null){
            viewWrapper.addView(child);
        }
        return viewWrapper;
    }

    protected FrameLayout createIndentedViewWrapper(){
        FrameLayout frameLayout = new FrameLayout(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getFromDp(getDisplayMetrics(), 20), 0, 0, 0);
        layoutParams.gravity = Gravity.LEFT;
        frameLayout.setLayoutParams(layoutParams);
        return frameLayout;
    }

    protected LinearLayout createLinearLayoutWrapper(){
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return linearLayout;
    }

    protected View createLayoutSpacer(){
        final View view = new View(getContext());
        final DisplayMetrics metrics = getDisplayMetrics();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, getFromDp(metrics, 1));
        params.setMargins(0, getFromDp(metrics,10),
                0, getFromDp(metrics,10));

        view.setLayoutParams(params);
        view.setBackgroundColor(getContext().getResources().getColor(R.color.textDarkSecondary, null));
        return view;
    }

    protected CheckBox createCheckBox(String preferenceKey, String text, boolean smallText, View subView){
        CheckBox cb = new CheckBox(getContext());
        cb.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cb.setText(text);

        if(smallText){
            cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }else{
//            cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }

        boolean selection = mPref.getBoolean(preferenceKey, false);
        cb.setChecked(selection); //Set initial selection state
        if(subView != null){
            DisplayUtils.setEnableStateRecursive(selection, subView);
        }

        cb.setOnClickListener(view -> {
            mPrefEditor.putBoolean(preferenceKey, cb.isChecked());
            if(subView != null){
                DisplayUtils.setEnableStateRecursive(cb.isChecked(), subView);
            }
        });
        return cb;
    }

    protected CheckBox createCheckBox(String preferenceKey, String text){
        return createCheckBox(preferenceKey, text, false, null);
    }

    protected Spinner createSpinner(String preferenceKey, String[] options) {
        return createSpinner(preferenceKey, options, null);
    }

    protected Spinner createSpinner(String preferenceKey, String[] options, String[] keys) {
        LinearLayout.LayoutParams paramsSpinner = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsSpinner.setMargins(0, getFromDp(getDisplayMetrics(),10),
                0, getFromDp(getDisplayMetrics(),10));

        Spinner spinner = new Spinner(getContext());
        spinner.setLayoutParams(paramsSpinner);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, options);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        int savedSelectionIdx = 0;
        if(keys != null){
            String str = mPref.getString(preferenceKey, keys[0]);
            for(int i = 0; i < keys.length; i++) {
                if(str.equals(keys[i])){
                    savedSelectionIdx = i;
                    break;
                }
            }
        }else {
            savedSelectionIdx = mPref.getInt(preferenceKey, 0);
        }
        spinner.setSelection(savedSelectionIdx);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(keys != null){
                    mPrefEditor.putString(preferenceKey, keys[position]);
                }else{
                    mPrefEditor.putInt(preferenceKey, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return spinner;
    }

    protected Spinner createStatusSpinner(String preferenceKey){
        JobStatus[] jobStatus = JobStatus.values();
        String[] names = new String[jobStatus.length];
        for (int i = 0; i < jobStatus.length; i++) {
            names[i] = getContext().getResources().getString(jobStatus[i].job_status_text_resource);
        }
        String[] keys = new String[jobStatus.length];
        for (int i = 0; i < jobStatus.length; i++) {
            keys[i] = jobStatus[i].toString();
        }

        return createSpinner(preferenceKey, names, keys);
    }

    protected TextView createTextView(@StringRes int res) {
        TextView tv = new TextView(getContext());
        tv.setText(res);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        return tv;
    }

    protected TextView createTextView(CharSequence text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        return tv;
    }

    protected EditText createEditTextView(String preferenceKey) {
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final EditText editText = new EditText(getContext());
        editText.setLayoutParams(layoutParams);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setMaxLines(1);
        editText.setSingleLine(true);

        String savedText = mPref.getString(preferenceKey, null);
        if(savedText != null)
            editText.setText(savedText);

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPrefEditor.putString(preferenceKey, String.valueOf(editText.getText()));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
        editText.addTextChangedListener(textWatcher);

        return editText;
    }

    protected RadioButton createRadioButton(@StringRes int textRes, RadioGroup radioGroup){
        DisplayMetrics m = DisplayUtils.getMetrics(getContext());
        final RadioButton rb = new RadioButton(getContext());
        rb.setId(View.generateViewId());
        rb.setText(textRes);
        rb.setPadding(getFromDp(m, 20),getFromDp(m, 8),
                getFromDp(m, 20),getFromDp(m, 8));
        TextViewCompat.setTextAppearance(rb,R.style.TextAppearance_AppCompat_Menu);
        final RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rb.setLayoutParams(layoutParams);
        if(radioGroup != null) {
            radioGroup.addView(rb);
        }
        return rb;
    }

    protected RadioButton createRadioButton(String text, RadioGroup radioGroup){
        DisplayMetrics m = DisplayUtils.getMetrics(getContext());
        final RadioButton rb = new RadioButton(getContext());
        rb.setId(View.generateViewId());
        rb.setText(text);
        rb.setPadding(getFromDp(m, 20),getFromDp(m, 8),
                getFromDp(m, 8),getFromDp(m, 8));
        TextViewCompat.setTextAppearance(rb,R.style.TextAppearance_AppCompat_Menu);
        final RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rb.setLayoutParams(layoutParams);
        if(radioGroup != null) {
            radioGroup.addView(rb);
        }
        return rb;
    }

    protected RadioGroup createRadioGroup(String preferenceKey){
        RadioGroup rg = new RadioGroup(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rg.setLayoutParams(layoutParams);
        return rg;
    }

    protected RadioGroup createRadioSelection(String preferenceKey, Pair<String, String>[] keys) {
        final RadioGroup rg = createRadioGroup(preferenceKey);

        String savedSelection = mPref.getString(preferenceKey, null);

        final int[] rbIds = new int[keys.length];
        for (int i = 0; i < keys.length; i++){
            Pair<String, String> key = keys[i];
            RadioButton rb = createRadioButton(key.second, rg);
            if (savedSelection != null && savedSelection.equals(key.first)) {
                rb.setChecked(true);
            }else if(i == 0 && savedSelection == null){
                rb.setChecked(true);
            }
            rbIds[i] = rb.getId();
        }

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = (RadioButton) rg.findViewById(group.getCheckedRadioButtonId());
            if (rb != null) {
                for (int i = 0; i < rbIds.length; i++) {
                    if (rbIds[i] == checkedId) {
                        mPrefEditor.putString(preferenceKey, keys[i].first);
                    }
                }
            }
        });
        return rg;
    }


}
