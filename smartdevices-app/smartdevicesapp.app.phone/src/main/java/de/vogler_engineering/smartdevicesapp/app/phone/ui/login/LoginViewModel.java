/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.login;

import androidx.lifecycle.MutableLiveData;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableDouble;
import androidx.databinding.ObservableField;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.model.repository.AuthRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractViewModel;
import timber.log.Timber;

/**
 * Created by vh on 09.02.2018.
 */

public class LoginViewModel extends AbstractViewModel {

    private static final String TAG = "LoginViewModel";

    @Inject
    AuthRepository authRepository;

    public final MutableLiveData<String> username   = new MutableLiveData<>();
    public final MutableLiveData<String> devicename = new MutableLiveData<>();
    public final MutableLiveData<String> password   = new MutableLiveData<>();
    public final MutableLiveData<String> hostname   = new MutableLiveData<>();
    public final MutableLiveData<Boolean> signInEnabled    = new MutableLiveData<>();

    public final ObservableField<String> message = new ObservableField<>("");

    public final ObservableBoolean signInInProgress = new ObservableBoolean(false);
    public final ObservableDouble signInProgress = new ObservableDouble(0);

    @Inject
    public LoginViewModel() {
        signInEnabled.setValue(true);

//        Observable.combineLatest(
//                username, devicename, password, hostname,
//                (username, devicename, password, hostname) ->
//                        isNotNullOrEmpty(username) &&
//                        isNotNullOrEmpty(devicename) &&
//                        isNotNullOrEmpty(password) &&
//                        isNotNullOrEmpty(hostname) &&
//                        isValidUrl(hostname))
//                .subscribe(signInEnabled::set);
    }

    public void signIn(){
        Timber.tag(TAG).i("SignIn clicked: %s, %s, %s", username.getValue(), devicename.getValue(), password.getValue());

        if(!signInEnabled.getValue())
            return;

        //1. GET AUTH SERVER

        //2. GET AUTH SERVER INFO

        //3. GET AUTH TOKEN

        //4. GET DEVICE INFO & DEVICE CONFIG

        //5. REDIRECT

    }

    public boolean isSignedIn(){
        return authRepository.isSignedIn();
    }

}
