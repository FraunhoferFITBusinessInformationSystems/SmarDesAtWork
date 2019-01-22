/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.sampledata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.common.util.RxUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.job.JobStatus;
import de.vogler_engineering.smartdevicesapp.model.entities.message.MessageReply;
import de.vogler_engineering.smartdevicesapp.model.entities.requests.FilterRequest;
import de.vogler_engineering.smartdevicesapp.model.entities.requests.PaginationRequest;
import de.vogler_engineering.smartdevicesapp.model.entities.requests.SortRequest;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MessageRepository;
import io.reactivex.Single;

public class ChatMessageTester {

    private static final String TAG = "ChatMessageTester";

    private static final String CHAT_SEND_JOB_KEY = "ChatSendMessage";
    private static final String CHAT_CONFIRM_JOB_KEY = "ChatConfirmMessage";

    private static final String CHAT_START_ACTION_TEXT = "StartJob";
    private static final String CHAT_START_ = "StartJob";

    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;
    private final JobRepository jobRepository;
    private final MessageRepository messageRepository;

    public ChatMessageTester(AppManager appManager, SchedulersFacade schedulersFacade, JobRepository jobRepository, MessageRepository messageRepository) {
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
        this.jobRepository = jobRepository;
        this.messageRepository = messageRepository;

    }

    public Single<UUID> StartChatJob() {
        Map<String, Object> props = new HashMap<>();
        MessageReply msg = messageRepository.createReplyMessage(CHAT_START_ACTION_TEXT, CHAT_SEND_JOB_KEY, props);
        return messageRepository.sendMessageReply(msg)
                .map(b -> {
                    if (b) {
                        return msg.getId();
                    }
                    return null;
                });
    }

    public Single<String> SendChatJob(String receiver, String message, UUID sendJobId) {
        Map<String, String> resources = new HashMap<>();
        resources.put("subject", receiver);
        resources.put("text", message);

        return jobRepository.sendConfigurableJob(CHAT_SEND_JOB_KEY, resources, sendJobId)
                .subscribeOn(schedulersFacade.newThread());
    }

    public Single<String> AcceptChatMessage(UUID jobId, Map<String, String> resource) {
        resource.put("submit", null);
        return jobRepository.sendConfigurableJob(CHAT_CONFIRM_JOB_KEY, resource, jobId);
    }

    public Single<JobEntryDto> WaitForChatMessage() {
        FilterRequest fr = new FilterRequest();
        fr.addFilter("status", JobStatus.Done.toString(), true);
        return jobRepository.getJobs(new PaginationRequest(), new SortRequest(), fr)
                .subscribeOn(schedulersFacade.newThread())
                .flatMap(x -> {
                    if (x == null || x.size() == 0) {
                        return Single.error(new Exception());
                    }
                    for (JobEntryDto e : x) {
                        if (CHAT_CONFIRM_JOB_KEY.equals(e.getEntry().getName())) {
                            return Single.just(e);
                        }
                    }
                    return Single.error(new Exception());
                })
                .retryWhen(new RxUtils.RetryWithDelay(3, 1000));
    }


    public Single<JobEntryDto> WaitForChatMessage(UUID jobId, int retry, int delay) {
        return jobRepository.getJob(jobId)
                .subscribeOn(schedulersFacade.newThread())
                .flatMap(x -> {
                    if (x == null) {
                        return Single.error(new Exception());
                    }
                    return Single.just(x);
                })
                .retryWhen(new RxUtils.RetryWithDelay(retry, delay));
    }
}
