/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import android.app.Dialog;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Locale;

import de.vogler_engineering.smartdevicesapp.common.misc.Consumer;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import timber.log.Timber;

public class NumberInputComponent extends BaseComponent<Integer> {

    private static final String TAG = "NumberInputComponent";

    private LinearLayout mLayout;
    private TextView mLabel;
    private TextView mTextView;
    private View mSpacer;

    public NumberInputComponent() {
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
    public void bindView(Context context, ComponentData<Integer> componentData, LifecycleOwner lifecycleOwner) {
        final NumberInputComponentData data = ComponentData.getComponentData(componentData, NumberInputComponentData.class);
        data.updatePropertySettings();

        mLabel.setText(element.getName());
        updateText(data);
        data.valueLiveData().observeForever((d) -> updateText(data));

        mTextView.setOnClickListener(l -> {
            Dialog d = createDialog(data.getCount(), data.getInterval(), data::setValue, mLayout.getContext(), element.getName());
            d.show();
        });
    }

    private void updateText(NumberInputComponentData data){
        mTextView.setText(String.format(Locale.getDefault(), "%d%s",
                data.getFormattedValue(), data.getSuffix()!=null?data.getSuffix():""));
    }

    private static Dialog createDialog(int count, int interval, Consumer<Integer> changeListener, Context context, String title) {
        final Dialog d = new Dialog(context);
        d.setContentView(R.layout.dialog_number_picker);
        d.setTitle(title);

        Button ba = d.findViewById(R.id.dialog_button_accept);
        final NumberPicker np = d.findViewById(R.id.dialog_number_picker_picker);

        np.setMaxValue(count-1);
        np.setMinValue(0);
        np.setDisplayedValues(getDisplayedValues(count, interval));
        np.setWrapSelectorWheel(false);

        ba.setOnClickListener(v -> {
            Timber.tag(TAG).i("Value set to: %s", String.valueOf(np.getValue()));
            changeListener.apply(np.getValue());
            d.dismiss();
        });
        return d;
    }

    private static String[] getDisplayedValues(int count, int interval) {
        String[] arr = new String[count];
        for(int i = 0; i < count; i++){
            arr[i] = String.valueOf(i * interval);
        }
        return arr;
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