/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.rx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides various threading schedulers.
 */
@Singleton
public class SchedulersFacade {

    @Inject
    public SchedulersFacade() {
    }

    /**
     * IO thread pool scheduler
     */
    public Scheduler io() {
        return Schedulers.io();
    }

    /**
     * Computation thread pool scheduler
     */
    public Scheduler computation() {
        return Schedulers.computation();
    }

    /**
     * Main Thread scheduler
     */
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    /**
     * A new thread scheduler
     */
    public Scheduler newThread(){
        return Schedulers.newThread();
    }

    /**
     * A new thread scheduler
     */
    private Scheduler backgroundServiceScheduler;

    public Scheduler backgroundService(){
        if(backgroundServiceScheduler == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            backgroundServiceScheduler = Schedulers.from(executor);
        }
        return backgroundServiceScheduler;
    }
}