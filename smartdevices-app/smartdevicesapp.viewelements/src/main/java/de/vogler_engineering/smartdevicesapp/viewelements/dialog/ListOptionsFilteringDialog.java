/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.common.util.MapUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.job.JobStatus;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DisplayUtils;

public class ListOptionsFilteringDialog extends AlertDialog.Builder {

    private SharedPreferences.OnSharedPreferenceChangeListener onStateFilterListOptionsPreferencesChangedListener;
    private static final Map<String,String> sortItems = new HashMap<String,String>();

    private Context mContext;
    private SharedPreferences mPref;
    private String mListType = "";
    private SharedPreferences.Editor mEditor;

    public ListOptionsFilteringDialog(Context context, String listType) {
        super(context);
        mContext = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPref.edit();
        mListType = listType;

        sortItems.put("Status","Status");
        sortItems.put("CreatedAt","Erstellt");
        sortItems.put("JobKey","Typ");
    }


    @Override
    public AlertDialog show() {
        this.createDialog();
        return super.show();
    }

    private void createDialog() {

        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.options_filter_dialog_layout, null);

        this.setTitle("Filter");
        this.setView(dialogView);

        this.setupOtions(dialogView,sortItems);

        this.setPositiveButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mEditor.apply();
            }
        });
        this.setNegativeButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("Canceled");
                    }
                });

        this.create();
    }


    public void setupOtions(View view , Map<String,String> items) {

        LinearLayout scrollViewLayout = (LinearLayout) view.findViewById(R.id.scrollView_layout);
        addLine(scrollViewLayout,1);
        addStateFilter(scrollViewLayout);
        addLine(scrollViewLayout,1);
        // addFiltering(scrollViewLayout);

    }

    private void addStateFilter(LinearLayout mainLayout){

        String varNameFilterState = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_STATE_ON_OFF);
        String varNameFilterStateBy = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_STATE_BY);
        String varNameFilterStateExcluding = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_STATE_EXCLUDING);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),40),
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),0,0);

        LinearLayout filterStateLay = new LinearLayout(mContext);
        filterStateLay.setOrientation(LinearLayout.VERTICAL);
        filterStateLay.setLayoutParams(params);

        CheckBox cb2 = createCheckBox(varNameFilterStateExcluding,
                mPref.getBoolean(varNameFilterStateExcluding, true),
                getContext().getResources().getString(R.string.option_filter_excluding),
                15.5f);
        filterStateLay.addView(cb2);

        LinearLayout.LayoutParams paramsSpinner = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsSpinner.setMargins(0,
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),0,
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10));

        Spinner spinner = new Spinner(mContext);
        spinner.setLayoutParams(paramsSpinner);
        JobStatus[] jobStatus = JobStatus.values();
        String[] values = new String[jobStatus.length];
        for (int i = 0; i < jobStatus.length; i++) {
            values[i] = getContext().getResources().getString(jobStatus[i].job_status_text_resource);
        };

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, values); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        String savedFilterStateName = mPref.getString(varNameFilterStateBy, null);

        if(savedFilterStateName != null){
            int index = JobStatus.valueOf(savedFilterStateName).ordinal();
            spinner.setSelection(index);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEditor.putString(varNameFilterStateBy, JobStatus.values()[position].name());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        filterStateLay.addView(spinner);

        LinearLayout.LayoutParams paramsCbMain = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCbMain.setMargins(DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),
                0,0,0);

        CheckBox cb1 = createCheckBox(varNameFilterState,
                mPref.getBoolean(varNameFilterState, false),
                getContext().getResources().getString(R.string.option_filter_state),
                20.0f, filterStateLay);
        cb1.setLayoutParams(paramsCbMain);
        mainLayout.addView(cb1);

        mainLayout.addView(filterStateLay);
    }

    private void addFiltering(LinearLayout mainLayout){

        String varNameFilterExcluding = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_EXCLUDING);
        String varNameFilterBy = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_BY);
        String varNameFilterString= PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_STRING);
        String varNameFilterOnOff= PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.FILTER_ON_OFF);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),40),
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),0,0);

        LinearLayout filterLay = new LinearLayout(mContext);
        filterLay.setOrientation(LinearLayout.VERTICAL);
        filterLay.setLayoutParams(params);

        CheckBox cb2 = createCheckBox(varNameFilterExcluding,
                mPref.getBoolean(varNameFilterExcluding, true),
                getContext().getResources().getString(R.string.option_filter_excluding),
                15.5f);
        //cb1.setPadding(50,20,8,8);
        filterLay.addView(cb2);

        LinearLayout.LayoutParams paramsSpinner = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsSpinner.setMargins(0,
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),0,
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10));

        Spinner spinner = new Spinner(mContext);
        spinner.setLayoutParams(paramsSpinner);

        List<String> values = new ArrayList<String>();
        for(String val : sortItems.values()){
            values.add(val);
        };

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, values); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        String savedFilterStateName = mPref.getString(varNameFilterBy, null);

        String s = sortItems.get(savedFilterStateName);
        int idx = values.indexOf(sortItems.get(savedFilterStateName));
        if(idx>=0 && idx<values.size())
            spinner.setSelection(idx);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String key = MapUtils.getKeyByValue(sortItems,parent.getItemAtPosition(position).toString());
                mEditor.putString(varNameFilterBy, key);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        filterLay.addView(spinner);

        LinearLayout.LayoutParams paramsCbMain = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCbMain.setMargins(DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),
                0,0,0);

        CheckBox cb1 = createCheckBox(varNameFilterOnOff,
                mPref.getBoolean(varNameFilterOnOff, false),
                getContext().getResources().getString(R.string.option_filter),
                20.0f, filterLay);
        cb1.setLayoutParams(paramsCbMain);
        mainLayout.addView(cb1);

        mainLayout.addView(filterLay);
    }

    private void addLine(LinearLayout lay, int height){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        params.setMargins(0,
                DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10),
                0, DisplayUtils.getFromDp(DisplayUtils.getMetrics(mContext),10));

        View v = new View(mContext);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                height
        ));
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
        v.setLayoutParams(params);
        lay.addView(v);
    }

    private CheckBox createCheckBox(String preferenceVariableName, Boolean preSelect, String text, float textSize, View viewToEnableDisable){

        if(viewToEnableDisable != null){
            DisplayUtils.setEnableStateRecursive(preSelect, viewToEnableDisable);
        }

        CheckBox cb = new CheckBox(mContext);
        if(textSize != -1) cb.setTextSize(textSize);
        cb.setText(text);
        cb.setChecked(preSelect);
        cb.setOnClickListener(new CheckBox.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                mEditor.putBoolean(preferenceVariableName, cb.isChecked());
                if(viewToEnableDisable != null){
                    DisplayUtils.setEnableStateRecursive(cb.isChecked(), viewToEnableDisable);
                }
            }
        });
        return cb;
    }

    private CheckBox createCheckBox(String preferenceVariableName, Boolean preSelect, String text){
        return createCheckBox( preferenceVariableName,preSelect, text, -1, null);
    }

    private CheckBox createCheckBox(String preferenceVariableName, Boolean preSelect, String text, float textSize){
        return createCheckBox(preferenceVariableName,preSelect, text, textSize, null);
    }

    private CheckBox createCheckBox(String preferenceVariableName, Boolean preSelect, String text, View viewToEnableDisable){
        return createCheckBox(preferenceVariableName,preSelect, text, -1, viewToEnableDisable);
    }


}
