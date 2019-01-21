/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import android.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.vogler_engineering.smartdevicesapp.common.misc.Consumer;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class OptionDialogComponent extends BaseComponent<ArrayList<String>> {

    private static final String TAG = "OptionComponent";

    private final String delimiter = ";";

    private LinearLayout mLayout;
    private TextView mLabel;
    private TextView mTextView;
    private View mSpacer;

    public OptionDialogComponent() {
        super(ComponentType.NumberInput);
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
        mTextView.setMinHeight(getFromDp(met,12));
        mLayout.addView(mTextView);

        mSpacer = new View(context);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getFromDp(met,1));
        spacerParams.setMargins(0, 0, 0, getFromDp(met, 8));
        mSpacer.setLayoutParams(spacerParams);
        mSpacer.setBackgroundColor(context.getResources().getColor(R.color.textDarkSecondary, null));
        mLayout.addView(mSpacer);

        return mLayout;
    }

    @Override
    public void bindView(Context context, ComponentData<ArrayList<String>> componentData, LifecycleOwner lifecycleOwner) {
        final OptionDialogComponentData data = ComponentData.getComponentData(componentData, OptionDialogComponentData.class);
        data.setDelimiter(delimiter);
        data.updatePropertySettings();

        mLabel.setText(element.getName());
        updateText(data);
        data.valueLiveData().observeForever((d) -> updateText(data));

        mTextView.setOnClickListener(l -> {
            AlertDialog d = createDialog(data.getItems(),data::setValue);
            d.show();
        });
    }

    private void updateText(OptionDialogComponentData data){
        mTextView.setText(data.getResourceValue());
    }

    public AlertDialog createDialog(String[] items, Consumer<ArrayList<String>> changeListener) {

        ArrayList selectedIndexes = new ArrayList();

        boolean[] states = setPreselection(items, selectedIndexes);

        AlertDialog.Builder builder = new AlertDialog.Builder(mLayout.getContext());
        builder.setTitle(element.getName())
                .setMultiChoiceItems(items, states,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    selectedIndexes.add(which);
                                } else if (selectedIndexes.contains(which)) {
                                    selectedIndexes.remove(Integer.valueOf(which));
                                }
                            }
                        })

                .setPositiveButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<String> selUsers = new ArrayList<String>();
                        for(int i= 0; i < selectedIndexes.size(); i++){
                            selUsers.add(items[(int) selectedIndexes.get(i)]);
                        }
                        changeListener.apply(selUsers);
                    }
                })
                .setNegativeButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("Canceled");
                    }
                });

        return builder.create();
    }

    private boolean[] setPreselection(String[] values, ArrayList selectedIndexes){
        boolean[] states = new boolean[values.length];
        String selected = mTextView.getText().toString();

        for (int i=0;i<values.length;i++) {
            if(selected.contains(values[i])){
                states[i] = true;
                selectedIndexes.add(i);
            }
        }
        return states;
    }



//    @Override
//    protected void updateData(String s) {
//        mTextView.setText(s);
//    }

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