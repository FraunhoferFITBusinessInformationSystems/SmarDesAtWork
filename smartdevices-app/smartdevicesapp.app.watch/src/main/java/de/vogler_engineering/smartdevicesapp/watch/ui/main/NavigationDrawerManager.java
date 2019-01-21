/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

import java.util.ArrayList;

import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.watch.R;
import de.vogler_engineering.smartdevicesapp.watch.ui.settings.SettingsActivity;
import de.vogler_engineering.smartdevicesapp.watch.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.watch.ui.AbstractWearableActivity;
import lombok.Getter;

public class NavigationDrawerManager implements WearableNavigationDrawerView.OnItemSelectedListener {


    private final ArrayList<DrawerEntry> drawerEntries = new ArrayList<>();
    private final AbstractWearableActivity activity;
    private final WearableNavigationDrawerView mWearableNavigationDrawerView;
    private NavigationAdapter mNavigationAdapter;

    public NavigationDrawerManager(AbstractWearableActivity activity,
                                   WearableNavigationDrawerView wearableNavigationDrawerView) {

        this.activity = activity;
        this.mWearableNavigationDrawerView = wearableNavigationDrawerView;
    }

    @Override
    public void onItemSelected(int position) {
        DrawerEntry entry = drawerEntries.get(position);

        if (entry == DrawerEntry.SETTINGS_ENTRY){
            Intent intent = new Intent(activity, SettingsActivity.class);
            activity.startActivity(intent);
        }else if (entry == DrawerEntry.UPDATE_ENTRY){
            Intent intent = UpdateService.createStartUpdateServiceIntent(
                    activity,
                    Constants.ACTIONS.GET_ALL_NOTIFICATION);
            activity.startService(intent);
        }
    }

    public void initializeDrawer() {
        drawerEntries.clear();
        drawerEntries.add(DrawerEntry.SETTINGS_ENTRY);
        drawerEntries.add(DrawerEntry.UPDATE_ENTRY);

        mNavigationAdapter = new NavigationAdapter(activity);
        mWearableNavigationDrawerView.setAdapter(mNavigationAdapter);
        mWearableNavigationDrawerView.getController().peekDrawer();
        mWearableNavigationDrawerView.addOnItemSelectedListener(this);
    }

    private final class NavigationAdapter
            extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

        private final Context mContext;

        public NavigationAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return drawerEntries.size();
        }

        @Override
        public String getItemText(int pos) {
            return mContext.getResources().getString(drawerEntries.get(pos).getNameRes());
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            int navigationIcon = drawerEntries.get(pos).getIconRes();
            return mContext.getDrawable(navigationIcon);
        }
    }

    public static final class DrawerEntry {
        public final static DrawerEntry SETTINGS_ENTRY = new DrawerEntry(
                R.string.drawer_entry_settings,
                R.drawable.ic_settings_white_60dp);

        public final static DrawerEntry UPDATE_ENTRY = new DrawerEntry(
                R.string.drawer_entry_update,
                R.drawable.ic_sync_white_60dp);

        @Getter
        @StringRes
        private int nameRes;
        @Getter
        @DrawableRes
        private int iconRes;

        public DrawerEntry(@StringRes int nameRes,
                           @DrawableRes int navigationIconRes) {
            this.nameRes = nameRes;
            this.iconRes = navigationIconRes;
        }
    }

}
