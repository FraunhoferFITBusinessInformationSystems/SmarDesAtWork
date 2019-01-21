/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.integration;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.common.test.categories.IntegrationTests;
import de.vogler_engineering.smartdevicesapp.model.entities.EchoResult;
import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;
import de.vogler_engineering.smartdevicesapp.model.mock.RepositoryMockManager;
import de.vogler_engineering.smartdevicesapp.model.repository.EchoRepository;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.EchoRestService;
import io.reactivex.Single;
import timber.log.Timber;

@Category(IntegrationTests.class)
public class EchoRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String TAG = "EchoRepositoryIntegrationTest";

    RepositoryMockManager rmm;

    @Before
    public void before() throws Exception {
        //Setup Repositories
        rmm = new RepositoryMockManager();
        rmm.init();
    }

    @Test
    public void testEchoRequestRepoOnline() {
        EchoRestService service = rmm.getRestServiceProvider().createRestService(EchoRestService.class);
        SchedulersFacade schedulersFacade = rmm.getSchedulersFacade();

        Semaphore waitHandle = new Semaphore(0);

        EchoRepository echoRepo = new EchoRepository(
                rmm.getRestServiceProvider(),
                rmm.getAppManager(),
                rmm.getSchedulersFacade());

        Single<EchoResult> echoResultSingle = echoRepo.echoRequest().observeOn(schedulersFacade.io());
        echoResultSingle.subscribe(
                echoResult -> {
                    Assert.assertNotNull(echoResult);
                    System.out.format("echo %s - %d - Up: %d - Down: %d", echoResult.getOnlineState().toString(), echoResult.getMs(), echoResult.getMsUp(), echoResult.getMsDown());
                    Assert.assertEquals(OnlineState.ONLINE, echoResult.getOnlineState());
                    waitHandle.release();
                },
                (err) -> {
                    Timber.tag(TAG).e(err);
                    Assert.fail("Exception: " + err.toString());
                    waitHandle.release();
                }
        );

        long start = System.currentTimeMillis();
        boolean fi = false;
        while((System.currentTimeMillis()-start) <= 10000 && !fi ){
            try {
                if(waitHandle.tryAcquire(1000, TimeUnit.MILLISECONDS)){
                    fi = true;
                }
            }catch (InterruptedException e){
            }
        }
        if(!fi){
            Assert.fail("Timeout occurred!");
        }
    }

    @Test
    public void testEchoRequestRepoOffline() {
        rmm.getAppManagerMock().baseUrl.postValue("http://localhost:9999");
        EchoRestService service = rmm.getRestServiceProvider().createRestService(EchoRestService.class);
        SchedulersFacade schedulersFacade = rmm.getSchedulersFacade();

        Semaphore waitHandle = new Semaphore(0);

        EchoRepository echoRepo = new EchoRepository(
                rmm.getRestServiceProvider(),
                rmm.getAppManager(),
                rmm.getSchedulersFacade());

        Single<EchoResult> echoResultSingle = echoRepo.echoRequest().observeOn(schedulersFacade.io());
        echoResultSingle.subscribe(
                echoResult -> {
                    Assert.assertNotNull(echoResult);
                    System.out.format("echo %s - %d - Up: %d - Down: %d", echoResult.getOnlineState().toString(), echoResult.getMs(), echoResult.getMsUp(), echoResult.getMsDown());
                    Assert.assertEquals(OnlineState.OFFLINE, echoResult.getOnlineState());
                    waitHandle.release();
                },
                (err) -> {
                    Timber.tag(TAG).e(err);
                    waitHandle.release();
                    Assert.fail("Exception: " + err.toString());
                }
        );

        long start = System.currentTimeMillis();
        boolean fi = false;
        while((System.currentTimeMillis()-start) <= 10000 && !fi ){
            try {
                if(waitHandle.tryAcquire(1000, TimeUnit.MILLISECONDS)){
                    fi = true;
                }
            }catch (InterruptedException e){
            }
        }
        if(!fi){
            Assert.fail("Timeout occurred!");
        }
    }

}
