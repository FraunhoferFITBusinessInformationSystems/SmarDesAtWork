/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui;

import android.app.ActivityOptions;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.UUID;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.job.JobActivity;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.main.MainActivity;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.settings.SettingsActivity;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.todostep.TodoStepActivity;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.Navigator;
import timber.log.Timber;

/**
 * A utility class that handles navigation for the app.
 */
public class NavigationController implements Navigator {
    private static final String TAG = "NavigationController";

    private final static String PATH_KEY_MAIN = "main";
    private final static String PATH_KEY_SETTINGS = "settings";
    private final static String PATH_KEY_JOB = "job";
    private final static String PATH_KEY_TODO = "todo";
    private final static String PATH_KEY_ABOUT = "about";

    private final MutableLiveData<String> currentTab = new MutableLiveData<>();
    private final MutableLiveData<String> currentPath = new MutableLiveData<>();

    private MainTabChangeCallback mainTabChangeCallback = null;

//    private FragmentManager fragmentManager;
    private final AppManager appManager;

    @Inject
    public NavigationController(AppManager appManager) {
        this.appManager = appManager;
        this.appManager.setNavigator(this);

        setCurrentPath(PATH_KEY_MAIN, "dashboard");
        currentTab.setValue("dashboard");
    }

    public void init(MainActivity mainActivity){
//        if(fragmentManager == null) {
//            this.fragmentManager = mainActivity.getSupportFragmentManager();
//        }
    }

    @Override
    public void navigateToJob(Context context, UUID id) {
        //TODO not implemented
        Timber.tag(TAG).e("navigateToJob(UUID) Not Implemented!");
    }

    @Override
    public void navigateToJob(String jobKey, UUID id) {
        Context mc = appManager.getMainContext();
        navigateToJob(mc, jobKey, id);
    }

    @Override
    public void navigateToJob(Context context, String jobKey, UUID id){
        setCurrentPath(PATH_KEY_JOB, String.format("%s/%s", jobKey, id));

        Intent intent = new Intent(context, JobActivity.class);
        Bundle bundle = new Bundle();
        if(id != null) {
            bundle.putString(JobActivity.PARAM_JOB_ID, id.toString());
        }
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void navigateToSettings(Context context) {
        setCurrentPath(PATH_KEY_SETTINGS);
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToMainTab(Context context, String tabKey) {
        String currentPath = getCurrentPath();
        setCurrentPath(PATH_KEY_MAIN, tabKey);
        if (currentPath.startsWith(PATH_KEY_MAIN) && mainTabChangeCallback != null) {
            // only notify drawer manager
            mainTabChangeCallback.navigateToTab(tabKey);
        } else {
            // start activity with intent
            Intent intent = new Intent(context, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.PARAM_TAB_KEY, tabKey);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    @Override
    public void navigateToMain(Context context){
        String currentPath = getCurrentPath();
        setCurrentPath(PATH_KEY_MAIN);
        if(currentPath.startsWith(PATH_KEY_MAIN) && mainTabChangeCallback != null){
            // only notify drawer manager
            mainTabChangeCallback.navigateToMainTab();
        }else{
            // start activity with intent
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void navigateToAbout(Context context) {
        //setCurrentPath(PATH_KEY_ABOUT);
    }

    @Override
    public void navigateToListDetails(Context context, String listId, UUID instanceId, int stepNumber, String contextDomain, int animationDirection) {
        setCurrentPath(PATH_KEY_TODO, String.format("%s/%s/%s", listId, instanceId, stepNumber));

        Intent intent = new Intent(context, TodoStepActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        if(instanceId != null) {
            bundle.putString(TodoStepActivity.PARAM_INST_ID, instanceId.toString());
        }
        bundle.putString(TodoStepActivity.PARAM_LIST_ID, listId);
        bundle.putString(TodoStepActivity.PARAM_CONTEXT_DOMAIN, contextDomain);
        bundle.putInt(TodoStepActivity.PARAM_STEP_NUM, stepNumber);
        intent.putExtras(bundle);

        ActivityOptions options;
        switch (animationDirection){
            case Navigator.ANIMATION_DIRECTION_UP:
            case Navigator.ANIMATION_DIRECTION_END:
                options =
                        ActivityOptions.makeCustomAnimation(context, R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case Navigator.ANIMATION_DIRECTION_DEFAULT:
            case Navigator.ANIMATION_DIRECTION_DOWN:
            case Navigator.ANIMATION_DIRECTION_START:
            default:
                options =
                        ActivityOptions.makeCustomAnimation(context, R.anim.slide_from_left, R.anim.slide_to_right);
        }
        context.startActivity(intent, options.toBundle());
    }

//    public FragmentManager getFragmentManager() {
//        if(fragmentManager == null)
//            throw new IllegalStateException("NavigationController not yet initialized!");
//        return fragmentManager;
//    }

    private void setCurrentPath(String activity) {
        Timber.tag(TAG).i("Setting current tag from=%s to=%s", currentPath.getValue(), activity);
        this.currentPath.postValue(activity);
    }

    private void setCurrentPath(String activity, String instance) {
        Timber.tag(TAG).i("Setting current tag from=%s to=%s", currentPath.getValue(), activity);
        this.currentPath.postValue(String.format("%s/%s", activity, instance));
    }

    public String getCurrentPath() {
        return currentPath.getValue();
    }

    public LiveData<String> getCurrentPathObservable(){
        return currentPath;
    }

    public void setMainTabChangeCallback(MainTabChangeCallback mainTabChangeCallback) {
        this.mainTabChangeCallback = mainTabChangeCallback;
    }

    public MainTabChangeCallback getMainTabChangeCallback() {
        return mainTabChangeCallback;
    }

    public interface MainTabChangeCallback{
        void navigateToMainTab();
        void navigateToTab(String tabKey) throws IllegalArgumentException;
    }
}