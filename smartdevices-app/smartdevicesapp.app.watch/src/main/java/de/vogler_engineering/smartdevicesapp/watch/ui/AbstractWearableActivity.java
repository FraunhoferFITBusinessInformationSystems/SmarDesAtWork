/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.watch.ui;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.EchoResult;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceInfo;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.BaseServiceManager;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DeviceInfoRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.EchoRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.Constants;
import de.vogler_engineering.smartdevicesapp.viewelements.SmartDevicesApplication;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.FirebaseRelayMessageHandler;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DeviceInfoUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

public abstract class AbstractWearableActivity extends AppCompatWearableActivity implements FirebaseRelayMessageHandler.FirebaseMessageReceiver {

    private static final String TAG = "AbstractActivity";

    private static boolean appRunning = false;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private FirebaseRelayMessageHandler firebaseRelayMessageHandler;

    private Observable<Long> echoObservable;
    private Disposable echoDisposable;

    @Getter
    @Setter
    private boolean echoEnabled = false;

    private boolean resumed = false;
    private boolean running = false;

    @Getter
    private boolean initialLoad = true;

    @Getter
    private boolean startedFromNotification;

    @Inject
    AppManagerImpl appManager;

    @Inject
    SchedulersFacade schedulersFacade;

    @Inject
    BaseServiceManager serviceManager;

    @Inject
    ConfigRepository configRepository;

    @Inject
    EchoRepository echoRepository;

    @Inject
    DeviceInfoRepository deviceInfoRepository;

    @Inject
    DynamicValueRepository dynamicValueRepository;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void setStartedFromNotification(boolean b) {
        startedFromNotification = b;
    }

    protected <VM extends AbstractViewModel> VM loadViewModel(Class<VM> clazz) {
        if (viewModelFactory == null)
            throw new RuntimeException("No viewmodel factory injected!");

        return ViewModelProviders.of(this, viewModelFactory).get(clazz);
    }

    public void registerContextReceivers() {
        firebaseRelayMessageHandler.registerContextReceivers();

        if (echoEnabled) {
            if (echoObservable == null) {
                echoObservable = Observable
                        .interval(Constants.VALUES.ECHO_POLLING_INTERVAL, Constants.VALUES.ECHO_POLLING_TIMEUNIT)
                        .observeOn(schedulersFacade.newThread())
                        .doOnNext(n -> makeEchoRequest());
            }
            echoDisposable = echoObservable.subscribe();
            disposables.add(echoDisposable);
        } else {
            initialLoad = false;
        }

        if (dynamicValueRepository.isPollingEnabled()) {
            dynamicValueRepository.startPolling();
        }
    }

    public void unregisterContextReceivers() {
        firebaseRelayMessageHandler.unregisterContextReceivers();

        if (echoDisposable != null && !echoDisposable.isDisposed()) {
            echoDisposable.dispose();
            disposables.remove(echoDisposable);
        }

        dynamicValueRepository.stopPolling();
    }

    private <R> ObservableSource<? extends R> makeEchoRequest() {
        EchoResult echoResult = echoRepository.echoRequest()
                .blockingGet();
        initialLoad = false;
        serviceManager.setConnectionState(
                echoResult.getOnlineState(), echoResult.getMs(), echoResult.getRequestDate());
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        firebaseRelayMessageHandler = new FirebaseRelayMessageHandler(this, this, appManager, configRepository);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String action = extras.getString("action");
                firebaseRelayMessageHandler.resolveBundleAction(action);
            }
        }

        super.onCreate(savedInstanceState);
    }

    public void storeFirebaseToken(String token) {
        DeviceInfo deviceInfo = DeviceInfoUtils.buildDeviceInfo(this);
        deviceInfo.setFcmToken(token);
        deviceInfoRepository.updateDeviceInfo(deviceInfo);
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return;
            }
        }
        Timber.tag(TAG).e("Could't close soft-keyboard!");
    }

    @Override
    protected void onDestroy() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
        super.onDestroy();
        setActualContext(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        running = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        setAppRunning(true);
        setActualContext(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        setAppRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
    }

    public boolean isActivityResumed() {
        return resumed;
    }

    public boolean isActivityRunning() {
        return running;
    }

    public void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    private static void setAppRunning(boolean state){
        SmartDevicesApplication.setAppRunning(state);
    }

    private static void setActualContext(Context context){
        SmartDevicesApplication.setActualContext(context);
    }
}
