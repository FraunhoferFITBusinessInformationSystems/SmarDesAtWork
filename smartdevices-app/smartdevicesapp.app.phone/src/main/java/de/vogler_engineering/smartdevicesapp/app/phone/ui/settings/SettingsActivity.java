/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;

public class SettingsActivity extends AppCompatActivity implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    protected SettingsViewModel viewModel = null;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    RestServiceProvider restServiceProvider;

    @Inject
    UiMessageUtil uiMessageUtil;

    @Inject
    AppManager appManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel.class);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.title_settings));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener( this::onPreferencesChanged);

        UserPreferenceFragment userPrefs = UserPreferenceFragment.create();
        androidx.fragment.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.settings_container, userPrefs);
        fragmentTransaction.commit();

        viewModel.getActivityFinishedObservable().observe(this, (v) -> this.finish());
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
