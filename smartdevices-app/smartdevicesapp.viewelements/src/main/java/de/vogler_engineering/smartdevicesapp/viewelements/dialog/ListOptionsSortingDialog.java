/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.core.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;

public class ListOptionsSortingDialog extends AlertDialog.Builder {

    private static final Map<String,String> sortItems = new HashMap<String,String>();

    private static final int idAsc = 1000;
    private static final int idDec = 1001;

    private Context mContext;
    private AlertDialog mAlertDialog;
    private SharedPreferences mPref;
    private String mListType = "";
    private SharedPreferences.Editor mEditor;

    public ListOptionsSortingDialog(Context context, String listType) {
        super(context);
        mContext = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPref.edit();
        mListType = listType;

        sortItems.put("Status","Status");
        sortItems.put("Datum","CreatedAt");
        sortItems.put("Typ","JobKey");
    }


    @Override
    public AlertDialog show() {

        this.createDialog();
        mAlertDialog = super.show();
        return mAlertDialog;
    }

    private void createDialog() {

        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.options_sort_dialog_layout, null);

        this.setTitle("Sortierung");
        this.setView(dialogView);

        this.addRadioButtons(dialogView,sortItems);

        this.setPositiveButton(de.vogler_engineering.smartdevicesapp.viewelements.R.string.dialog_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mEditor.apply();
            }
        });
        this.setNegativeButton(de.vogler_engineering.smartdevicesapp.viewelements.R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("Canceled");
                    }
                });

        this.create();
    }


    public void addRadioButtons(View view , Map<String,String> items) {

        RadioGroup rgpAscDec = (RadioGroup) view.findViewById(R.id.radio_group_ascdec);
        if(rgpAscDec == null){
            return;
        }

        RadioButton rbnAsc = new RadioButton(mContext);
        rbnAsc.setId(idAsc);
        rbnAsc.setText("Absteigend");
        rbnAsc.setPadding(8,20,8,20);
        TextViewCompat.setTextAppearance(rbnAsc,R.style.TextAppearance_AppCompat_Menu);

        RadioButton rbnDec = new RadioButton(mContext);
        rbnDec.setId(idDec);
        rbnDec.setText("Aufsteigend");
        rbnDec.setPadding(8,20,8,20);
        TextViewCompat.setTextAppearance(rbnDec,R.style.TextAppearance_AppCompat_Menu);

        rgpAscDec.addView(rbnAsc);
        rgpAscDec.addView(rbnDec);

        String varNameSortAsc = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.SORT_ASC);
        if(mPref.getBoolean(varNameSortAsc, true)){
            rgpAscDec.check(idDec);
        } else {
            rgpAscDec.check(idAsc);
        }

        rgpAscDec.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String varName = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.SORT_ASC);
                if(checkedId == idAsc){
                    mEditor.putBoolean(varName, false);
                } else {
                    mEditor.putBoolean(varName, true);
                }
            }
        });

        RadioGroup rgp = (RadioGroup) view.findViewById(R.id.radio_group);
        if(rgp == null){
            return;
        }

        int buttonId = 0;
        for (Map.Entry<String, String> entry : items.entrySet()) {
            RadioButton rbn = new RadioButton(mContext);
            rbn.setId(buttonId);
            rbn.setText(entry.getKey());
            rbn.setPadding(8,20,8,20);
            TextViewCompat.setTextAppearance(rbn,R.style.TextAppearance_AppCompat_Menu);
            rgp.addView(rbn);

            String varNameSortBy = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.SORT_BY);
            String prefVal = mPref.getString(varNameSortBy, "");

            if(prefVal.equals(entry.getValue().toString())){
                rgp.check(buttonId);
            }
            buttonId++;
        }

        rgp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) view.findViewById(group.getCheckedRadioButtonId());
                if(rb == null){
                    return;
                }
                String varName = PreferenceUtils.getPrefVariableName(mListType,PreferenceUtils.SORT_BY);
                mEditor.putString(varName, items.get(rb.getText()));
            }
        });
    }

}
