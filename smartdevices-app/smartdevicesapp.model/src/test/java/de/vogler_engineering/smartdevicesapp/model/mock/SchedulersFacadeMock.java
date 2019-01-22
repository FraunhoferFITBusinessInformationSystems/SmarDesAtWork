/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import lombok.NonNull;

public class SchedulersFacadeMock extends SchedulersFacade{

    @Override
    public Scheduler io() {
        return super.io();
    }

    @Override
    public Scheduler computation() {
        return super.computation();
    }

    @Override
    public Scheduler ui() {
        return super.ui();
    }

    @Override
    public Scheduler newThread() {
        return super.newThread();
    }

    @Override
    public Scheduler backgroundService() {
        return super.backgroundService();
    }

    public void initializeTestDefaults() {
        Scheduler immediate = new Scheduler() {
            @Override
            public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
                // this prevents StackOverflowErrors when scheduling with a delay
                return super.scheduleDirect(run, 0, unit);
            }

            @Override
            public Worker createWorker() {
                return new ExecutorScheduler.ExecutorWorker(Runnable::run);
            }
        };

        Scheduler test = new TestScheduler();

        RxJavaPlugins.setInitIoSchedulerHandler(scheduler -> createMultiThreadExecutorScheduler(4));
        RxJavaPlugins.setInitComputationSchedulerHandler(scheduler -> createMultiThreadExecutorScheduler(4));
        RxJavaPlugins.setInitNewThreadSchedulerHandler(scheduler -> createNewThreadScheduler());
        RxJavaPlugins.setInitSingleSchedulerHandler(scheduler -> createSingleThreadExecutorScheduler());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> createSingleThreadExecutorScheduler());
    }

    private Scheduler createNewThreadScheduler(){
        ExecutorService service = Executors.newCachedThreadPool();
        return Schedulers.from(service);
    }

    private Scheduler createSingleThreadExecutorScheduler(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        return Schedulers.from(service);
    }

    private Scheduler createMultiThreadExecutorScheduler(int threads){
        ExecutorService service = Executors.newFixedThreadPool(threads);
        return Schedulers.from(service);
    }

    private Scheduler createScheduledThreadExecutorScheduler(int threads){
        ExecutorService service = Executors.newScheduledThreadPool(threads);
        return Schedulers.from(service);
    }
}
