/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.management;

import android.content.Context;

import java.util.UUID;

public interface Navigator {

    /**
     * Navigates to the job-activity with the given parameters using the given context.
     *
     * @param context The context which is used to create the navigation intent.
     * @param id The UUID of the Job
     */
    void navigateToJob(Context context, UUID id);

    /**
     * Navigates to the job-activity with the given parameters using the main context from the
     * {@link AppManager}
     *
     * @param jobKey The JobKey of the Job
     * @param id The UUID of the Job
     */
    void navigateToJob(String jobKey, UUID id);

    /**
     * Navigates to the job-activity with the given parameters using the given context
     *
     * @param context The context which is used to create the navigation intent.
     * @param jobKey The JobKey of the Job
     * @param id The UUID of the Job
     */
    void navigateToJob(Context context, String jobKey, UUID id);

    /**
     * Navigates to the settings-activity using the given context
     * @param context The context which is used to create the navigation intent.
     */
    void navigateToSettings(Context context);

    /**
     * Navigates to the about-activity using the given context
     * @param context The context which is used to create the navigation intent.
     */
    void navigateToAbout(Context context);

    /**
     * Navigates to the default tab in the main-activity using the given context.
     * @param context The context which is used to create the navigation intent.
     */
    void navigateToMain(Context context);

    /**
     * Navigates to the Tab with the specified <code>String tabKey</code> using the given context.
     * @param context The context which is used to create the navigation intent.
     * @param tabKey The tab identifier
     */
    void navigateToMainTab(Context context, String tabKey);

    /**
     * Navigates to the TodoListDetailsActivity for the given Step, Key and Instance,
     * using the given context.
     * @param context The context which is used to create the navigation intent.
     * @param listId The TodoList-Identifier
     * @param instanceId The uuid of the current TodoList-instance
     * @param stepNumber The selected step-number in the TodoList
     */
    default void navigateToListDetails(Context context, String listId, UUID instanceId, int stepNumber, String contextDomain){
        navigateToListDetails(context, listId, instanceId, stepNumber, contextDomain, ANIMATION_DIRECTION_DEFAULT);
    }

    /**
     * Navigates to the TodoListDetailsActivity for the given Step, Key and Instance,
     * using the given context.
     * @param context The context which is used to create the navigation intent.
     * @param listId The TodoList-Identifier
     * @param instanceId The uuid of the current TodoList-instance
     * @param stepNumber The selected step-number in the TodoList
     */
    void navigateToListDetails(Context context, String listId, UUID instanceId, int stepNumber, String contextDomain, int animationDirection);


    int ANIMATION_DIRECTION_DEFAULT = 0;
    int ANIMATION_DIRECTION_UP = 1;
    int ANIMATION_DIRECTION_END = 2;
    int ANIMATION_DIRECTION_DOWN = 3;
    int ANIMATION_DIRECTION_START = 4;


}
