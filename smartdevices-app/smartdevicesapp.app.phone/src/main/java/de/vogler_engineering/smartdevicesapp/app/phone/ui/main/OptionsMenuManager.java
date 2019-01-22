/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.app.phone.service.UpdateService;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs.GenericTabFragment;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.dialog.ConfigurableFilterDialogBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.dialog.ConfigurableSortDialogBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.dialog.DialogButtonOptions;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import io.reactivex.Single;
import timber.log.Timber;

/**
 * Class to manage the OptionsMenu on an Activity. Dynamically generates the entries based on
 * config and starts some generated dialogs.
 */
public class OptionsMenuManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "OptionsMenuManager";

    private final AbstractActivity activity;
    private final AppManagerImpl appManager;
    private final TabPagerManager tabPagerManager;
    private SchedulersFacade schedulersFacade;

    public OptionsMenuManager(AbstractActivity activity, AppManagerImpl appManager, TabPagerManager tabPagerManager, SchedulersFacade schedulersFacade) {
        this.activity = activity;
        this.appManager = appManager;
        this.tabPagerManager = tabPagerManager;
        this.schedulersFacade = schedulersFacade;
    }

    public void initOptionsMenu(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    public boolean inflateOptionsMenu(Menu menu){
        activity.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("CheckResult")
    public boolean handleOptionsMenuItemSelected(MenuItem item){
        int id = item.getItemId();

        int tabIdx = tabPagerManager.getCurrentTabIndex();
        GenericTabFragment fragment = tabPagerManager.getTabFragment(tabIdx);
        TabConfig config = tabPagerManager.getTabConfig(tabIdx);

        if (id == R.id.action_filter) {

            Single.fromCallable(() -> new ConfigurableFilterDialogBuilder(activity, config.getKey()))
                    .subscribeOn(schedulersFacade.newThread())
                    .observeOn(schedulersFacade.ui())
                    .map(x -> {
                        x.setupDialog(config.getFilterEntries(),
                                activity.getResources().getString(R.string.configurable_filter_dialog, config.getTitle()),
                                DialogButtonOptions.OkCancel);
                        return x;
                    })

                    .subscribe(
                            AlertDialog.Builder::show,
                            (err) -> Timber.tag(TAG).e(err, "Could not create FilterDialog!"));
        }else if (id == R.id.action_sort) {

            Single.fromCallable(() -> new ConfigurableSortDialogBuilder(activity, config.getKey()))
                    .subscribeOn(schedulersFacade.newThread())
                    .observeOn(schedulersFacade.ui())
                    .map(x -> {
                        x.setupDialog(config.getSortEntries(),
                                activity.getResources().getString(R.string.configurable_sort_dialog, config.getTitle()),
                                DialogButtonOptions.OkCancel);
                        return x;
                    })
                    .subscribe(
                            AlertDialog.Builder::show,
                            (err) -> Timber.tag(TAG).e(err, "Could not create SortDialog!"));
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.startsWith(PreferenceUtils.LIST_DIALOG_PREFIX)) {
            Intent newIntent = UpdateService.createStartUpdateServiceIntent(activity,
                    Constants.ACTIONS.GET_DATA_NOTIFICATION);
            appManager.getAppContext().startService(newIntent);
        }
    }
}
