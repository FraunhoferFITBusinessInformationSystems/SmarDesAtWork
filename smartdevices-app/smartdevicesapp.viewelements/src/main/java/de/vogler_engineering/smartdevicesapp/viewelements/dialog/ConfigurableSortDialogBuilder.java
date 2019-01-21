/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.dialog;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabSortEntry;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DisplayUtils;

public class ConfigurableSortDialogBuilder extends ConfigurablePreferenceEditDialogBuilder {

    private static final String TAG = "ConfigurableFilterDialogBuilder";

    private final static String SORT_TYPE_STATE = "state";
    private final static String SORT_TYPE_TEXT = "text";
    private final static String SORT_TYPE_TYPE = "type";
    private final static String SORT_TYPE_DATE = "date";

    public ConfigurableSortDialogBuilder(Context context, String preferencePrefix) {
        super(context, preferencePrefix, PreferenceUtils.MENU_TYPE_SORT);
    }

    public void setupDialog(List<TabSortEntry> entries, String title, DialogButtonOptions buttonOptions) {
        super.setupDialog(title, buttonOptions);
        View view = null;

        addEditOption(createLayoutSpacer());

        //add Sort-Entries
        if(entries.size() != 0){

            //Always add Sort-Orientation
            //addEditOption(createTextView(R.string.configurable_dialog_sort_orientation));

            Pair[] pairs = new Pair[]{
                    new Pair<>(PreferenceUtils.VALUE_ASC, getContext().getResources().getString(R.string.configurable_dialog_sort_orientation_asc)),
                    new Pair<>(PreferenceUtils.VALUE_DESC, getContext().getResources().getString(R.string.configurable_dialog_sort_orientation_desc))
            };

            //noinspection unchecked
            view = createRadioSelection(getPreferenceKey(PreferenceUtils.VALUE_NAME_ASC), pairs);
            addEditOption(view);

            addEditOption(createLayoutSpacer());

            //noinspection unchecked
            Pair<String, String>[] pairs2 = new Pair[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                TabSortEntry entry = entries.get(i);
                pairs2[i] = new Pair<>(entry.getKey(), entry.getName());
            }

            view = createRadioSelection(getPreferenceKey(PreferenceUtils.VALUE_NAME_KEY), pairs2);
            addEditOption(view);

        }else{
            TextView tv = createTextView(R.string.configurable_dialog_no_sorts);
            int pxh = DisplayUtils.getFromDp(getDisplayMetrics(), 20);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)tv.getLayoutParams();
            layoutParams.setMargins(pxh, pxh, pxh, pxh);
            tv.setTextColor(getContext().getResources().getColor(R.color.textDarkSecondary, null));
            addEditOption(tv);
        }

        addEditOption(createLayoutSpacer());
    }
}
