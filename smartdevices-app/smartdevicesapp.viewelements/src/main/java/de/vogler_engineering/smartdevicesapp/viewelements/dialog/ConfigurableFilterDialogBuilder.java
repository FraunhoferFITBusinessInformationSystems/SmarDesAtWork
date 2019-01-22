/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.dialog;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabFilterEntry;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DisplayUtils;
import timber.log.Timber;

public class ConfigurableFilterDialogBuilder extends ConfigurablePreferenceEditDialogBuilder {

    private static final String TAG = "ConfigurableFilterDialogBuilder";
    
    private final static String FILTER_TYPE_STATUS = "state";
    private final static String FILTER_TYPE_TEXT = "text";
    private final static String FILTER_TYPE_TYPE = "type";
    private final static String FILTER_TYPE_DATE = "date";

    public ConfigurableFilterDialogBuilder(Context context, String preferencePrefix) {
        super(context, preferencePrefix, PreferenceUtils.MENU_TYPE_FILTER);
    }

    public void setupDialog(List<TabFilterEntry> entries, String title, DialogButtonOptions buttonOptions) {
        super.setupDialog(title, buttonOptions);
        View view = null;

        addEditOption(createLayoutSpacer());

        int filterCount = 0;
        for (TabFilterEntry entry : entries) {
            switch (entry.getFilterType()) {
                case FILTER_TYPE_STATUS: view = createStatusFilterView(entry); break;
                case FILTER_TYPE_TEXT: view = createTextFilterView(entry); break;
                case FILTER_TYPE_TYPE: view = createTypeFilterView(entry); break;
                case FILTER_TYPE_DATE: view = createDateFilterView(entry); break;
                default:
                    view = null;
            }

            if(view == null){
                Timber.tag(TAG).e("Unknown FilterEntry \"%s\" with key: \"%s\"", entry.getFilterType(), entry.getKey());
            }else {
                addEditOption(view);
                filterCount++;
            }
        }
        if(filterCount == 0){
            TextView tv = createTextView(R.string.configurable_dialog_no_filters);
            int px = DisplayUtils.getFromDp(getDisplayMetrics(), 20);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)tv.getLayoutParams();
            layoutParams.setMargins(px, px, px, px);
            tv.setTextColor(getContext().getResources().getColor(R.color.textDarkSecondary, null));
            addEditOption(tv);
        }

        addEditOption(createLayoutSpacer());
    }


    private View createStatusFilterView(TabFilterEntry entry) {
        LinearLayout inner = createLinearLayoutWrapper();
        if(entry.isInvertable()){
            CheckBox invertCb = createCheckBox(getPreferenceKey(entry.getKey(), PreferenceUtils.POSTFIX_INVERTED),
                    getContext().getResources().getString(
                            R.string.configurable_dialog_inverted_cb_text),
                    true, null);
            inner.addView(invertCb);
        }

        Spinner spinner = createStatusSpinner(getPreferenceKey(entry.getKey()));
        inner.addView(spinner);

        FrameLayout wrapper = createIndentedViewWrapper(inner);
        CheckBox statusCb = createCheckBox(getPreferenceKey(entry.getKey(), PreferenceUtils.POSTFIX_ENABLED), entry.getName(), false, wrapper);

        LinearLayout outer = createLinearLayoutWrapper();
        outer.addView(statusCb);
        outer.addView(wrapper);
        return outer;
    }

    private View createTextFilterView(TabFilterEntry entry) {
        EditText editText = createEditTextView(getPreferenceKey(entry.getKey()));

        FrameLayout wrapper = createIndentedViewWrapper(editText);
        CheckBox statusCb = createCheckBox(getPreferenceKey(entry.getKey(), PreferenceUtils.POSTFIX_ENABLED), entry.getName(), false, wrapper);
        LinearLayout outer = createLinearLayoutWrapper();
        outer.addView(statusCb);
        outer.addView(wrapper);
        return outer;
    }


    private View createTypeFilterView(TabFilterEntry entry) {
        return null;
    }

    private View createDateFilterView(TabFilterEntry entry) {
        return null;
    }
}
