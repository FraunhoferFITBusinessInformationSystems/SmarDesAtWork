/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.Navigator;
import de.vogler_engineering.smartdevicesapp.watch.ui.job.JobActivity;
import de.vogler_engineering.smartdevicesapp.watch.ui.main.MainActivity;

public class NavigationController implements Navigator {

    private final AppManager appManager;

    public NavigationController(AppManager appManager) {
        this.appManager = appManager;
        this.appManager.setNavigator(this);

//        setCurrentPath(PATH_KEY_MAIN, "dashboard");
//        currentTab.setValue("dashboard");
    }

    public void init(MainActivity mainActivity){
    }

    @Override
    public void navigateToJob(Context context, UUID id) {
        Intent intent = new Intent(context, JobActivity.class);
        Bundle bundle = new Bundle();
        if(id != null) {
            bundle.putString(JobActivity.PARAM_JOB_ID, id.toString());
        }
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void navigateToJob(String jobKey, UUID id) {
    }

    @Override
    public void navigateToJob(Context context, String jobKey, UUID id){
    }

    @Override
    public void navigateToSettings(Context context) {

    }

    @Override
    public void navigateToAbout(Context context) {

    }

    @Override
    public void navigateToMain(Context context) {

    }

    @Override
    public void navigateToMainTab(Context context, String tabKey) {

    }

    @Override
    public void navigateToListDetails(Context context, String listId, UUID instanceId, int stepNumber, String contextDomain, int animationDirection) {

    }
}
