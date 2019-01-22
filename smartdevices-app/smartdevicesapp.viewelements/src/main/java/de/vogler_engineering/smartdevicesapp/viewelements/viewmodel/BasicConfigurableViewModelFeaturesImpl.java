/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.viewmodel;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutViewModelReference;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import io.reactivex.Single;
import timber.log.Timber;

public class BasicConfigurableViewModelFeaturesImpl implements ConfigurableViewModelFeatures {

    private static final String TAG = "BasicConfigurableViewModelFeaturesImpl";

    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;
    private final UiMessageUtil uiMessageUtil;
    private final JobRepository jobRepository;
    private final LayoutViewModelReference reference;

    private JobEntryDto jobEntry;

    public BasicConfigurableViewModelFeaturesImpl(AppManager appManager, SchedulersFacade schedulersFacade, UiMessageUtil uiMessageUtil, JobRepository jobRepository, LayoutViewModelReference reference) {
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
        this.uiMessageUtil = uiMessageUtil;
        this.jobRepository = jobRepository;
        this.reference = reference;
    }

    public void setJobEntry(JobEntryDto entry) {
        jobEntry = entry;
    }

    @Override
    public void sendJob() {
        sendJob(new HashMap<>());
    }

    public void sendJob(Map<String,String> res) {
        if(jobEntry == null || jobEntry.getEntry() == null){
            throw new IllegalStateException("JobEntry is not set but used!");
        }
        final String jobKey = jobEntry.getEntry().getName();
        reference.postLoadingState(ActivityLoadingState.Loading);
        reference.addDisposable(Single.fromCallable(() -> reference.getDataHelper().collectResources(res))
                .subscribeOn(schedulersFacade.newThread())
                .flatMap((resources) -> jobRepository.sendConfigurableJob(jobKey, resources, jobEntry.getEntry().getId()))
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        result -> {
                            Timber.tag(TAG).i("Job %s successfully sent!", jobKey);
                            uiMessageUtil.makeToast(R.string.job_toast_sending_successfull);
                            reference.finishActivity();
                        },
                        e -> {
                            Timber.tag(TAG).e(e, "Error while sending job %s!", jobKey);
                            uiMessageUtil.makeToast(R.string.job_toast_sending_unsuccessfull);
                            reference.finishActivity();
                        }));
    }

    @Override
    public void sendJobReply(String value) {
        if(jobEntry == null || jobEntry.getEntry() == null){
            throw new IllegalStateException("JobEntry is not set but used!");
        }
        final String jobKey = jobEntry.getEntry().getName();
        reference.postLoadingState(ActivityLoadingState.Loading);
        reference.addDisposable(Single.fromCallable(() -> reference.getDataHelper().collectResources())
                .subscribeOn(schedulersFacade.newThread())
                .flatMap((resources)
                        -> jobRepository.sendConfigurableJobReply(jobKey, resources, jobEntry.getEntry().getId())
                )
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        result -> {
                            Timber.tag(TAG).i("JobReply %s successfully sent!", jobKey);
                            uiMessageUtil.makeToast(R.string.job_toast_sending_successfull);
                            reference.finishActivity();
                        },
                        e -> {
                            Timber.tag(TAG).e(e, "Error while sending JobReply %s!", jobKey);
                            uiMessageUtil.makeToast(R.string.job_toast_sending_unsuccessfull);
                            reference.finishActivity();
                        }));
    }

    @Override
    public void removeJob() {
        if(jobEntry == null || jobEntry.getEntry() == null){
            throw new IllegalStateException("JobEntry is not set but used!");
        }
        final String jobKey = jobEntry.getEntry().getName();
        reference.postLoadingState(ActivityLoadingState.Loading);
        reference.addDisposable(Single.fromCallable(() -> reference.getDataHelper().collectResources())
                .subscribeOn(schedulersFacade.newThread())
                .flatMap((resources) -> jobRepository.sendRemoveJob(jobEntry.getEntry().getId()))
                .map(result -> {
                    if (!(result != null && result)) {
                        throw new Exception("Error while sending");
                    }
                    return true;
                })
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        result -> {
                            Timber.tag(TAG).i("Job cancelling for %s successfully sent!", jobKey);
                            uiMessageUtil.makeToast(R.string.job_toast_sending_canceled);
                            reference.finishActivity();
                        },
                        e -> {
                            Timber.tag(TAG).e(e, "Error while sending job %s!", jobKey);
                            uiMessageUtil.makeToast(R.string.job_toast_sending_unsuccessfull);
                            reference.finishActivity();
                        }));
    }

    @Override
    public void startJob(String jobKey) {
        //TODO fix message repository
//        Map<String, Object> props = new HashMap<>();
//        MessageReply msg = messageRepository.createReplyMessage("StartJob", jobKey, props);
//
//        navigationController.navigateToJob(jobKey, msg.getId());
//        disposables.add(messageRepository.sendMessageReply(msg)
//                .observeOn(schedulersFacade.io())
//                .subscribe(
//                        (b) -> {
//                            if (b) {
//                                Timber.tag(TAG).i("MessageReply sent!");
//                            }
//                        },
//                        (e) -> Timber.tag(TAG).e(e, "Error while sending MessageReply")
//                ));
    }

    @Override
    public void openJob(String jobKey, UUID id) {
    }

    @Override
    public void startActivity(Intent intent, int errorText) {
        Context ctx = appManager.getMainContext();
        if(ctx == null){
            ctx = appManager.getAppContext();
        }
        final Context context = ctx;
        reference.addDisposable(Single.fromCallable(() -> true).subscribe((r) -> {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, errorText,  Toast.LENGTH_LONG).show();
                Timber.tag(TAG).e(e, "Could not start activity!");
            }
        }));
    }

    @Override
    public void openTodoListDetails(String listId, UUID instanceId, int stepNumber) {
        String domain = reference.getContextDomain();
        appManager.getNavigator().navigateToListDetails(appManager.getContext(), listId, instanceId, stepNumber, domain);
    }
}
