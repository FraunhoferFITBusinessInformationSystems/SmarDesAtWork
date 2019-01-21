/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.settings;

import android.app.FragmentTransaction;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.watch.R;

public class SettingsActivity extends AppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    protected SettingsViewModel viewModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        setTheme(android.R.style.Theme_DeviceDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel.class);
        setTitle(getString(R.string.title_settings));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this::onPreferencesChanged);

//        UserPreferenceFragment userPrefs = UserPreferenceFragment.create();
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.settings_container, userPrefs);
//        fragmentTransaction.commit();
//
        UserPreferenceFragment userPrefs = UserPreferenceFragment.create();
        androidx.fragment.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.settings_container, userPrefs);
        fragmentTransaction.commit();
    }

    private void onPreferencesChanged(SharedPreferences sharedPreferences, String key) {
        viewModel.updateSettings(sharedPreferences);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//    @OnClick(R.id.settings_submit_button)
//    void onSettingsSubmitClick(){
//        viewModel.submitSettings(this);
//    }
}
