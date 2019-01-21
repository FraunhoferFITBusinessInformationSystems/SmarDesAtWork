/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.job.Priority;
import de.vogler_engineering.smartdevicesapp.model.entities.message.MessageReply;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.ActionsRestService;
import io.reactivex.Single;
import timber.log.Timber;

public class MessageRepository extends AbstractRepository {

    private static final String TAG = "MessageRepository";
    
    private final RestServiceProvider restServiceProvider;

    public MessageRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        super(appManager, schedulersFacade);
        this.restServiceProvider = restServiceProvider;
    }

    public MessageReply createReplyMessage(String action, String name, Map<String, Object> properties){
        MessageReply msg = new MessageReply();
        msg.setId(UUID.randomUUID());
        msg.setPriority(Priority.Normal);
        msg.setCreatedBy(appManager.getDeviceIdKey());
        msg.setCreatedAt(new Date());
        msg.setType("JobReply");
        msg.setName(name);

        msg.setAction(action);
        msg.getAdditionalProperties().putAll(properties);

        return msg;
    }

    public Single<Boolean> sendMessageReply(MessageReply msg){
        ActionsRestService rs = restServiceProvider.createRestService(ActionsRestService.class);
        return rs.putMessageReply(appManager.getDeviceIdKey(), msg)
                .subscribeOn(schedulersFacade.newThread())
                .map(dd -> {
                    Timber.tag(TAG).d("Data updated!");
                    return true;
                });
    }


}
