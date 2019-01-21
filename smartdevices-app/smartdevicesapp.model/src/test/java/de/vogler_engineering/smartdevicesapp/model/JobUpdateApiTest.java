/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.util.Log;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.mock.RepositoryMockManager;
import de.vogler_engineering.smartdevicesapp.model.mock.TimberAssertingOnErrorTestRule;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class JobUpdateApiTest {

    private static final String TAG = "JobUpdateApiTest";

    RepositoryMockManager rmm;

    @Rule
    public TimberTestRule timberTestRule = TimberTestRule.builder()
            .showThread(true)
            .showTimestamp(true)
            .onlyLogWhenTestFails(false)
            .build();

    @Rule
    public TestRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public TimberAssertingOnErrorTestRule assertingOnErrorTestRule = TimberAssertingOnErrorTestRule.builder().minAssertPriority(Log.ERROR).build();

    @Before
    public void before() throws Exception {
        //Setup Repositories
        rmm = new RepositoryMockManager();
        rmm.init();


        rmm.getAppManagerMock().baseUrl.postValue("http://localhost:7000");
        rmm.getAppManagerMock().deviceId.postValue(new DeviceId("debug1.A"));
    }

//    @Test
    public void testGetJob2(){
        JobRepository jobRepository = rmm.getJobRepository();
        SchedulersFacade schedulersFacade = rmm.getSchedulersFacade();
        UUID idOk = UUID.fromString("54ea3f8b-307e-41e8-8e00-9f1f4ebcc725");
        final UUID jobId = idOk;

        CompositeDisposable disposables = new CompositeDisposable();
        final AtomicBoolean finished = new AtomicBoolean(false);

        jobRepository.getJob(jobId, 500)
                .subscribe(
                        (jobEntryDto) -> {
                            if (jobEntryDto == null) {
                                throw new IllegalArgumentException("Could not load Job:\"" + jobId + "\"");
                            }
                            Timber.tag(TAG).i("Successfully loaded Job: @%s", jobId);
                            finished.set(true);
                            Timber.tag(TAG).i("Successfully initialized JobComponent");
                        }
                );

        while(!finished.get()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        disposables.dispose();
    }


    public void testGetJob(){
        JobRepository jobRepository = rmm.getJobRepository();
        SchedulersFacade schedulersFacade = rmm.getSchedulersFacade();
        UUID idErr = UUID.fromString("54ea3f8b-307e-41e8-8e00-9f1f4ebcc724");
        UUID idOk = UUID.fromString("54ea3f8b-307e-41e8-8e00-9f1f4ebcc725");

        final AtomicBoolean finished = new AtomicBoolean(false);
        final UUID jobId = idOk;
        CompositeDisposable disposables = new CompositeDisposable();


        disposables.add(Observable.interval(250, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulersFacade.newThread())
                .map(l -> jobRepository.getJob(jobId)
                        .map(jobEntryDto -> jobEntryDto != null
                                && jobEntryDto.getEntry() != null)
                        .blockingGet())
                .takeWhile(x -> !x)
                .subscribe(
                        (x) -> {
                        },
                        (err) -> {
                            Timber.tag(TAG).e(err, "Error while initializing JobComponent");
                        },
                        () -> { //finally: load job
                            disposables.add(jobRepository.getJob(jobId)
                                    .map(jobEntryDto -> {
                                        if (jobEntryDto == null) {
                                            throw new IllegalArgumentException("Could not load Job:\"" + jobId + "\"");
                                        }
                                        Timber.tag(TAG).i("Successfully loaded Job: @%s", jobId);
                                        finished.set(true);
                                        return true;
                                    })
                                    .observeOn(schedulersFacade.ui())
                                    .subscribe(
                                            (x) -> {
                                                finished.set(true);
                                                Timber.tag(TAG).i("Successfully initialized JobComponent");
                                            },
                                            (err) -> {
                                                finished.set(true);
                                                Timber.tag(TAG).e(err, "Error while initializing JobComponent");
                                            }
                                    ));
                        }
                ));

        while(!finished.get()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        disposables.dispose();
    }
}
