/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.vogler_engineering.smartdevicesapp.app.phone.BuildConfig;
import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.ApkRestService;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.preferences.UserPreferenceFragmentBase;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * Created by vh on 08.11.2017.
 */

public class UserPreferenceFragment extends UserPreferenceFragmentBase {

    private static final String TAG = "UserPreferenceFragment";

    private final CompositeDisposable disposables = new CompositeDisposable();
    private AtomicBoolean updateCheckRunning = new AtomicBoolean(false);

    private SchedulersFacade schedulersFacade;
    private RestServiceProvider restServiceProvider;
    private UiMessageUtil uiMessageUtil;
    private  AppManager appManager;

    public UserPreferenceFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.user_preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if(preference.getKey().equals("pref_key_app_info_update")) {
            return this.checkForUpdates(preference);
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity a = getActivity();
        if(a instanceof SettingsActivity){
            SettingsActivity activity = (SettingsActivity) a;
            schedulersFacade = activity.schedulersFacade;
            restServiceProvider = activity.restServiceProvider;
            uiMessageUtil = activity.uiMessageUtil;
            appManager = activity.appManager;
        }

        //App Info Stuff
        Preference infoTextPref = findPreference("pref_key_app_info_text");
        String infoText = generateAppInfoText();
        infoTextPref.setSummary(infoText);
    }

    private boolean checkForUpdates(Preference preference) {
        if(updateCheckRunning.compareAndSet(true, true)){
            Timber.tag(TAG).i("Update check is already running! Cancel request.");
            return true;
        }

        Timber.tag(TAG).i("Search for updates!");

        ApkRestService restService = restServiceProvider.createRestService(ApkRestService.class);

        final int prefixLength = "de.vogler_engineering.".length();
        String apkName = BuildConfig.APPLICATION_ID.substring(prefixLength);
        final String currentApkName = apkName + '.' + BuildConfig.VERSION_NAME + ".apk";

        disposables.add(restService.getApkInfo()
                .subscribeOn(schedulersFacade.newThread())
                .map(apkInfo -> {
                    String latest = apkInfo.getLatest().getPhone();
                    return compareVersions(latest, currentApkName) > 0 ? latest : null;
                })
                .observeOn(schedulersFacade.ui())
                .subscribe(ver -> {
                    if(ver != null) {
                        askForUpdate(currentApkName, ver);
                    }else{
                        uiMessageUtil.makeToast(R.string.app_update_toast_already_uptodate);
                        updateCheckRunning.set(false);
                    }
                }, error -> {
                    Timber.tag(TAG).e(error, "Could not check for Updates!");
                    updateCheckRunning.set(false);
                }));
        return true;
    }

    private String newVersion;

    private void askForUpdate(String oldVersion, String newVersion){
        this.newVersion = newVersion;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.app_update_dialog_title);
        builder.setMessage(getContext().getResources().getString(R.string.app_update_dialog_message, newVersion));
        builder.setPositiveButton(R.string.dialog_yes, this::startUpdate);
        builder.setNegativeButton(R.string.dialog_no, (di, i) -> { /* nothing */
            updateCheckRunning.set(false);
        });
        builder.show();
    }

    private void startUpdate(DialogInterface dialogInterface, int i) {
        String url = appManager.getBaseUrl() + "/api/apk/" + newVersion;
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(url));
        startActivity(httpIntent);

        updateCheckRunning.set(false);
    }

    private String generateAppInfoText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Application-Id: ").append(BuildConfig.APPLICATION_ID).append('\n');
        sb.append("Build-Type: ").append(BuildConfig.BUILD_TYPE);
        sb.append(" (Flavor: ").append(BuildConfig.FLAVOR);
        sb.append(", Debug: ").append(BuildConfig.DEBUG).append(")\n");
        sb.append("Version: ").append(BuildConfig.VERSION_NAME);
        sb.append(" (Code: ").append(BuildConfig.VERSION_CODE).append(")\n");

        try{
            Date buildTime = BuildConfig.BUILD_TIME;
            SimpleDateFormat sdf = new SimpleDateFormat("E M d HH:mm:ss Z YYYY", Locale.getDefault());
            sb.append("Build-Date: ").append(sdf.format(buildTime)).append('\n');
        }catch (Exception ignored) { }

        return sb.toString();
    }

    private int compareVersions(String v1, String v2){
        Pair<String, Pair<int[], String>> p1 = splitVersion(v1);
        Pair<String, Pair<int[], String>> p2 = splitVersion(v2);
        int diff;

        if(p1 == null || p2 == null)
            return -1;
        diff = p1.first.compareTo(p2.first);
        if(diff != 0) return diff;

        for (int i = 0; i < 3; i++){
            int i1 = p1.second.first[i];
            int i2 = p2.second.first[i];

            diff = Integer.compare(i1, i2);
            if(diff != 0) return diff;
        }
        diff = p1.second.second.compareTo(p2.second.second);
        if(diff != 0) return diff;
        return 0;
    }

    private Pair<String, Pair<int[], String>> splitVersion(String v){
        try {
            String[] split = v.split("[-.]");
            int[] ver = new int[3];
            ver[0] = Integer.parseInt(split[3]);
            ver[1] = Integer.parseInt(split[4]);
            ver[2] = Integer.parseInt(split[5]);
            String type = split[6];
            String name = split[0] + "." + split[1] + "." + split[2];
            return new Pair<>(name, new Pair<>(ver, type));
        }catch (Exception ignored) {
            return null;
        }
    }

    public static UserPreferenceFragment create() {
        return new UserPreferenceFragment();
    }

    @Override
    public void onStop() {
        disposables.dispose();
        super.onStop();
    }
}
