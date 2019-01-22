/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import android.content.Context;

import com.squareup.picasso.Picasso;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;

public class ResourceRepositoryMock extends ResourceRepository {

    public ResourceRepositoryMock(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        super(appManager, schedulersFacade, restServiceProvider);
    }

    @Override
    protected void createPicasso(Context context) {
    }

    @Override
    protected Picasso getPicasso() {
        return null;
    }
}
