/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractViewModel;

public class SettingsViewModel extends AbstractViewModel {

    @Inject
    AppManager appManager;

    @Inject
    public SettingsViewModel() {
    }

    public void updateSettings(SharedPreferences pref){
        appManager.updateSettings(pref);
    }

    public void submitSettings(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        updateSettings(pref);
//        navigationController.navigateBack();
    }
}
