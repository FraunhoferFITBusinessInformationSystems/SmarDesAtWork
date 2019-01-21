/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.vogler_engineering.smartdevicesapp.model.entities.DeviceId;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.TabEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueData;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.interceptor.LoggingInterceptor;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.DataRestService;
import io.reactivex.Single;
import lombok.Getter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;

public class RestServiceProviderMock extends RestServiceProvider {

    @Getter
    private NetworkBehavior behavior;

    @Getter
    private MockRetrofit mockRetrofit;

    public RestServiceProviderMock(AppManager appManager) {
        super(appManager);
    }

    @Override
    public Retrofit createServices(String baseUrl){
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.readTimeout(5, TimeUnit.SECONDS);

        if(getAppManager().isLogRetrofit())
            httpClientBuilder.addNetworkInterceptor(new LoggingInterceptor());

        OkHttpClient client = httpClientBuilder.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(createJacksonMapper()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client);
        Retrofit retrofit = builder.build();

        // Create a MockRetrofit object with a NetworkBehavior which manages the fake behavior of calls.
        behavior = NetworkBehavior.create();
        mockRetrofit = new MockRetrofit.Builder(retrofit)
                .networkBehavior(behavior)
                .build();

        BehaviorDelegate<DataRestService> delegate = mockRetrofit.create(DataRestService.class);

        RestServiceProviderMock.MockDataRestService dataService = new RestServiceProviderMock.MockDataRestService(delegate);

        return retrofit;
    }





    static final class MockDataRestService implements DataRestService {

        private final BehaviorDelegate<DataRestService> delegate;
        private final Map<DeviceId, List<JobEntryDto>> allJobs;

        MockDataRestService(BehaviorDelegate<DataRestService> delegate) {
            this.delegate = delegate;
            allJobs = new LinkedHashMap<>();

            // Seed some mock data.
            readData();
        }


        @Override
        public Single<List<DynamicValueData>> getDynamicValueData(String deviceId) {
            return null;
        }

        @Override
        public Single<List<TabEntryDto>> getTabDataShort(String deviceId, String tabKey, Map<String, String> paginationRequest, Map<String, String> filterRequest, Map<String, String> sortRequest) {
            return null;
        }

//        @Override
//        public Single<List<Job>> getJobData(String deviceId, String sortBy, Boolean sortAsc, String filterBy, Boolean filterExcluding, String filterString, String filterStateBy, Boolean filterStateExcluding, int pageNumber, int pageSize) {
//            return null;
//        }
//
//        @Override
//        public Single<List<Message>> getMessageData(String deviceId) {
//            return null;
//        }
//
//        @Override
//        public Single<ActionResult> putJob(String deviceId, Job jobReply) {
//            return null;
//        }
//
//        @Override
//        public Single<ActionResult> clearDeviceJobs(String deviceId) {
//            return null;
//        }
//
//        @Override
//        public Single<ActionResult> putMessageReply(String deviceId, MessageReply reply) {
//            return null;
//        }
//
//        @Override
//        public Single<Map<DeviceId, List<JobEntryDto>>> getAllJobs(Map<String, String> filterRequest, Map<String, String> sortRequest) {
//            return null;
//        }
//
//        @Override
//        public Single<List<JobEntryDto>> getJobs(String deviceIdKey, Map<String, String> paginationRequest, Map<String, String> sortRequest, Map<String, String> filterRequest) {
//            List<JobEntryDto> response = Collections.emptyList();
//            List<JobEntryDto> jobs = allJobs.get(new DeviceId(deviceIdKey));
//            if (jobs != null) {
//                response = jobs;
//            }
//            return delegate.returningResponse(response).getJobs(deviceIdKey, paginationRequest, sortRequest, filterRequest);
//        }
//
//        @Override
//        public Single<JobEntryDto> getJob(String deviceIdKey, UUID jobId) {
//            JobEntryDto response = null;
//            List<JobEntryDto> jobs = allJobs.get(new DeviceId(deviceIdKey));
//            for (JobEntryDto job : jobs) {
//                if(job.getEntry() != null && job.getEntry().getId().equals(jobId)){
//                    response = job;
//                    break;
//                }
//            }
//            return delegate.returningResponse(response).getJob(deviceIdKey, jobId);
//        }
//
//        @Override
//        public Single<Boolean> removeJob(String deviceIdKey, UUID jobId) {
//            boolean response = false;
//            //ignore
//            return delegate.returningResponse(response).removeJob(deviceIdKey, jobId);
//        }


        public void readData(){
            try {
                byte[] jsonData = Files.readAllBytes(Paths.get("../testdata/JobRestServiceGetJobsResponse.json"));
                ObjectMapper objectMapper = new ObjectMapper();

                JobEntryDto[] jobs = objectMapper.readValue(jsonData, JobEntryDto[].class);
                allJobs.put(new DeviceId("debug1.A"), Arrays.asList(jobs));
            }catch (IOException e){
                Assert.assertNotNull("Error while reading data file", e);
            }
        }
    }
}
