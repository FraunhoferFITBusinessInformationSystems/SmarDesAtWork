/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.service;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.viewelements.service.AbstractUpdateService;

/**
 * Created by vh on 19.02.2018.
 */

public class UpdateService extends AbstractUpdateService {

    private static final String TAG = "UpdateService";

    @Inject
    public UpdateService() {
        super(UpdateService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Intent createStartUpdateServiceIntent(Context context, String action){
        Intent intent = new Intent(context, UpdateService.class);
        if(action != null){
            intent.setAction(action);
        }
        return intent;
    }
}
