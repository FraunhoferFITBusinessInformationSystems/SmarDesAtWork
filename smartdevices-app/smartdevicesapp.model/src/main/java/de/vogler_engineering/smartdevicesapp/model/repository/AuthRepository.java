/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.services.auth.TokenHandler;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;

/**
 * Created by vh on 12.02.2018.
 */

public class AuthRepository extends AbstractRepository {

    private final RestServiceProvider restServiceProvider;
    private final ConfigRepository configRepo;

    private TokenHandler tokenHandler;

    public final ObservableBoolean singedIn = new ObservableBoolean(false);
    public final ObservableField<String> currentAccessToken = new ObservableField<>();
    public final ObservableField<DeviceId> deviceId = new ObservableField<>();

    public AuthRepository(AppManagerImpl appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, ConfigRepository configRepo) {
        super(appManager, schedulersFacade);
        this.restServiceProvider = restServiceProvider;
        this.configRepo = configRepo;
    }

    public boolean isSignedIn() {
        return singedIn.get();
    }

    public void InitTokenHandler(String authorityUrl){
        tokenHandler = new TokenHandler(authorityUrl);
    }

    public void login(String username, String password) {
        tokenHandler.requestAccessToken(username, password);
    }
}
