/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.preferences.UserPreferenceFragmentBase;
import de.vogler_engineering.smartdevicesapp.watch.R;


/**
 * Created by vh on 08.11.2017.
 */
public class UserPreferenceFragment extends UserPreferenceFragmentBase {

    public UserPreferenceFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_general, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public static UserPreferenceFragment create() {
        return new UserPreferenceFragment();
    }
}
