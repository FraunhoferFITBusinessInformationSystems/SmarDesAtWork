/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import android.content.SharedPreferences;
import androidx.databinding.ObservableArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.misc.BiConsumer;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.common.util.MapUtils;
import de.vogler_engineering.smartdevicesapp.common.util.RxUtils;
import de.vogler_engineering.smartdevicesapp.model.builders.JobBuilder;
import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.ActionResult;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.TabEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.enums.JobType;
import de.vogler_engineering.smartdevicesapp.model.entities.requests.FilterRequest;
import de.vogler_engineering.smartdevicesapp.model.entities.requests.PaginationRequest;
import de.vogler_engineering.smartdevicesapp.model.entities.requests.SortRequest;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabFilterEntry;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.ActionsRestService;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.DataRestService;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.JobRestService;
import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import io.reactivex.Single;
import timber.log.Timber;

import static de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils.getPrefVariableName;

/**
 * Created by vh on 12.02.2018.
 */

public class JobRepository extends AbstractRepository {

    private static final String TAG = "JobRepository";


    private final RestServiceProvider restServiceProvider;
    private final TabRepository tabRepository;

    private final HashMap<String, ObservableArrayList<TabEntryDto>> tabEntries = new HashMap<>();
    private BiConsumer<String, Integer> tabCountListener = null;

    @Inject
    public JobRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider, TabRepository tabRepository) {
        super(appManager, schedulersFacade);
        this.restServiceProvider = restServiceProvider;
        this.tabRepository = tabRepository;
    }

    public ObservableArrayList<TabEntryDto> getEntries(String tabKey) {
        if (!this.tabEntries.containsKey(tabKey) || this.tabEntries.get(tabKey) == null) {
            this.tabEntries.put(tabKey, new ObservableArrayList<>());
        }
        return tabEntries.get(tabKey);
    }

    @Override
    public void updateData() {
        updateTabEntries();
    }

    private void updateTabEntries() {
        try {
            //Wait until UI-Thread has finished updating TabConfig.
            TabConfig[] tabConfig = Single.fromCallable(() -> {
                ObservableArrayList<TabConfig> tabConfigList = tabRepository.getTabConfig();
                return tabConfigList.toArray(new TabConfig[0]);
            }).subscribeOn(schedulersFacade.ui()).blockingGet();
            Timber.tag(TAG).d("Updating %d Tabs", tabConfig.length);
            List<Single<Boolean>> singles = new ArrayList<>();
            for (TabConfig c : tabConfig) {
                singles.add(updateTabData(c)
                        .subscribeOn(schedulersFacade.newThread())
//                        .delay(5, TimeUnit.SECONDS)
                        .map(x -> processTabEntries(x, c.getKey())));
            }
            @SuppressWarnings("unused")
            Boolean ignored = Single.zip(singles, r -> true)
                    .subscribeOn(schedulersFacade.newThread())
                    .blockingGet();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error while data update");
        }
    }

    private String getFilterStringValue(SharedPreferences pref, String varNameOnOffFilter, String varValueName, String defaultValue){
        String val = defaultValue;
        if(pref.getBoolean(getPrefVariableName(PreferenceUtils.LIST_JOBS,varNameOnOffFilter), false)){
            val = pref.getString(getPrefVariableName(PreferenceUtils.LIST_JOBS,varValueName), defaultValue);
        }
        return val;
    }

    private Boolean getFilterBooleanValue(SharedPreferences pref, String varNameOnOffFilter, String varValueName, Boolean defaultValue){
        Boolean val = defaultValue;
        if(pref.getBoolean(getPrefVariableName(PreferenceUtils.LIST_JOBS,varNameOnOffFilter), false)){
            val = pref.getBoolean(getPrefVariableName(PreferenceUtils.LIST_JOBS,varValueName), defaultValue);
        }
        return val;
    }

    private Single<List<TabEntryDto>> updateTabData(TabConfig tabConfig) {
        String tabKey = tabConfig.getKey();

        String[] filterNames = new String[tabConfig.getFilterEntries().size()];
        List<TabFilterEntry> filterEntries = tabConfig.getFilterEntries();
        for (int i = 0; i < filterEntries.size(); i++) {
            TabFilterEntry tabFilterEntry = filterEntries.get(i);
            filterNames[i] = tabFilterEntry.getKey();
        }
        SharedPreferences pref = appManager.getSharedPreferences();

        PaginationRequest pRequest = PaginationRequest.buildPaginationRequest(pref, tabKey);
        SortRequest sRequest = SortRequest.buildSortRequest(pref, tabKey);
        FilterRequest fRequest = FilterRequest.buildFilterRequest(pref, tabKey, filterNames);

        Map<String, String> pRequestParams = pRequest.buildRequest();
        Map<String, String> fRequestParams = fRequest.buildRequest();
        Map<String, String> sRequestParams = sRequest.buildRequest();

        Timber.tag(TAG).v("Make tab-request: %s - F:%s, S:%s, P:%s", tabKey,
                MapUtils.printMap(fRequestParams), MapUtils.printMap(sRequestParams), MapUtils.printMap(pRequestParams));

        return getDataRestService().getTabDataShort(
                appManager.getDeviceIdKey(),
                tabKey,
                pRequestParams,
                fRequestParams,
                sRequestParams);
    }

    private Boolean processTabEntries(List<TabEntryDto> newEntries, String tabKey) {
        ObservableArrayList<TabEntryDto> entries = getEntries(tabKey);

        Timber.tag(TAG).d("Received %d entries for Key: %s, processing....", newEntries != null ? newEntries.size() : 0, tabKey);
        //Merge Lists
        int num = entries.size() - newEntries.size();
        if (num > 0) {
            for (int j = 0; j < num; j++) {
                entries.remove(entries.size() - 1);
            }
        }

        for (int i = 0; i < newEntries.size(); i++) {
            if (i < entries.size()) {
                entries.set(i, newEntries.get(i));
            } else {
                entries.add(newEntries.get(i));
            }
        }
        if (num != 0) {
            notifyTabCountChangeListener(tabKey, newEntries.size());
        }
        return true;
    }

//    public Job get(UUID jobId) {
//        int index = findUUIDIndex(jobId);
//        if (index == -1) {
//            return null;
//        }
//        return jobs.get(index);
//    }

    private DataRestService getDataRestService() {
        return restServiceProvider.createRestService(DataRestService.class);
    }

    private ActionsRestService getActionsRestService() {
        return restServiceProvider.createRestService(ActionsRestService.class);
    }

    private JobRestService getJobRestService() {
        return restServiceProvider.createRestService(JobRestService.class);
    }

    public Single<String> sendConfigurableJob(String jobIdKey, Map<String, String> resource) {
        return sendConfigurableJob(jobIdKey, resource, null);
    }

    public Single<String> sendConfigurableJob(String jobIdKey, Map<String, String> resource,
                                              UUID referenceJob) {
        JobBuilder builder = new JobBuilder()
                .setCreator(appManager.getDeviceIdKey())
                .setJobType(JobType.Job)
                .setJobKey(jobIdKey)
                .setJobResource(resource);

        if (referenceJob != null)
            builder.setReferenceId(referenceJob);

        //Remove the old Job on sending
        //TODO: IF needed: Search all Tabs for this Job and remov it.
//        if (referenceJob != null) {
//            int index = findUUIDIndex(referenceJob);
//            if (index != -1) {
//                disposables.add(Single.fromCallable(() -> true)
//                        .subscribeOn(schedulersFacade.ui())
//                        .subscribe((v) -> jobs.remove(index)));
//            }
//        }

        return getActionsRestService().putJob(appManager.getDeviceIdKey(), builder.build())
                .map(ActionResult::getResult);
    }

    public Single<String> sendConfigurableJobReply(String jobReplyAction, Map<String, String> resource, UUID referenceId) {
        JobBuilder builder = new JobBuilder()
                .setCreator(appManager.getDeviceIdKey())
                .setReferenceId(referenceId)
                .setJobType(JobType.JobReply)
                .setJobReplyAction(jobReplyAction)
                .setJobResource(resource);

        return getActionsRestService().putJob(appManager.getDeviceIdKey(), builder.build())
                .map(ActionResult::getResult);
    }

    public Single<Boolean> sendRemoveJob(UUID jobId) {
        return getJobRestService().removeJob(appManager.getDeviceIdKey(), jobId);
    }

    public void setTabCountChangeListener(BiConsumer<String, Integer> listener) {
        tabCountListener = listener;
    }

    private void notifyTabCountChangeListener(String tabKey, int count) {
        if (tabCountListener != null)
            tabCountListener.apply(tabKey, count);
    }


    //
    // JobsEndpoint Specific Methods
    //

    public Single<Map<DeviceId, List<JobEntryDto>>> getAllJobs(SortRequest sortRequest,
                                                               FilterRequest filterRequest) {
        return getJobRestService().getAllJobs(
                filterRequest.buildRequest(),
                sortRequest.buildRequest());
    }


    public Single<List<JobEntryDto>> getJobs(PaginationRequest paginationRequest,
                                             SortRequest sortRequest,
                                             FilterRequest filterRequest) {
        return getJobRestService().getJobs(appManager.getDeviceIdKey(),
                paginationRequest.buildRequest(),
                sortRequest.buildRequest(),
                filterRequest.buildRequest());
    }

    public Single<JobEntryDto> getJob(UUID jobId) {
        return getJobRestService().getJob(appManager.getDeviceIdKey(), jobId);
    }

    public Single<Boolean> removeJob(UUID jobId) {
        return getJobRestService().removeJob(appManager.getDeviceIdKey(), jobId);
    }

    //
    // Additional JobEndpoint specifics
    //

    public Single<JobEntryDto> getJob(UUID id, int retry, int delayMs){
        return getJobRestService()
                .getJob(appManager.getDeviceIdKey(), id)
                .subscribeOn(schedulersFacade.newThread())
                .flatMap(x -> {
                    if (x == null) {
                        return Single.error(new Exception());
                    }
                    return Single.just(x);
                })
                .retryWhen(new RxUtils.RetryWithDelay(retry, delayMs));
    }

    public Single<JobEntryDto> getJob(UUID id, int delayMs) {
        return getJob(id, -1, delayMs);
    }
}
