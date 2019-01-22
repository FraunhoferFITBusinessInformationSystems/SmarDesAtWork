/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.common.util.DateUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.EchoResult;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.EchoRestService;
import io.reactivex.Single;
import retrofit2.Response;

public class EchoRepository extends AbstractRepository {

    private static final String TAG = "EchoRepository";

    private final RestServiceProvider restServiceProvider;

    @Inject
    public EchoRepository(RestServiceProvider restServiceProvider, AppManager appManager, SchedulersFacade schedulersFacade) {
        super(appManager, schedulersFacade);
        this.restServiceProvider = restServiceProvider;
    }

    public Single<EchoResult> echoRequest(){
        final DateFormat echoDateFormat = DateUtils.createJsonDateFormat();
        final long startTime = System.currentTimeMillis();
        EchoRestService service = restServiceProvider.createRestService(EchoRestService.class);

        return service.getEcho()
                .subscribeOn(schedulersFacade.newThread())
                .map(result -> {
                    final long endTime = System.currentTimeMillis();
                    final long ms = endTime - startTime;

                    try {
                        final Response<String> response = result.response();
                        if (result.isError()
                                || response == null
                                || !response.isSuccessful()
                                || response.body() == null) {
                            throw new Exception(result.error());
                        }

                        // FIXME: das mit der Zeit raushauen oder richtig parsen
                        return new EchoResult(ms, OnlineState.ONLINE, startTime, endTime, new Date(startTime), null);

//                        Date date = echoDateFormat.parse(response.body());
//                        long serverTime = date.getTime();
//
//                        return new EchoResult(ms, OnlineState.ONLINE, serverTime - startTime, endTime - serverTime, new Date(startTime), null);
                    } catch (Exception e){
                        return new EchoResult(ms, OnlineState.OFFLINE, -1, -1, new Date(startTime), e);
                    }
                })
                .observeOn(schedulersFacade.io());
    }
}
