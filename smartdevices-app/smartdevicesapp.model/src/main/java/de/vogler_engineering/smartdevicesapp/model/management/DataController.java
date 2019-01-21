/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.management;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceConfig;
import de.vogler_engineering.smartdevicesapp.model.repository.AbstractRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.DeviceInfoRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MediaRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;

@Singleton
public class DataController {

    private final ConfigRepository configRepository;
    private final JobRepository jobRepository;
    private final MediaRepository mediaRepository;
    private final ResourceRepository resourceRepository;
    private final DeviceInfoRepository deviceInfoRepository;

    private final List<AbstractRepository> repositories;
    private final TabRepository tabRepository;

    private boolean initialized = false;
    private boolean configUpdateRequested = false;

    @Inject
    public DataController(ConfigRepository configRepository,
                          TabRepository tabRepository,
                          JobRepository jobRepository,
                          MediaRepository mediaRepository,
                          ResourceRepository resourceRepository,
                          DeviceInfoRepository deviceInfoRepository) {
        this.configRepository = configRepository;
        this.tabRepository = tabRepository;
        this.jobRepository = jobRepository;
        this.mediaRepository = mediaRepository;
        this.resourceRepository = resourceRepository;
        this.deviceInfoRepository = deviceInfoRepository;

        repositories = new ArrayList<>();
        repositories.add(configRepository);
        repositories.add(tabRepository);
        repositories.add(jobRepository);
        repositories.add(mediaRepository);
        repositories.add(resourceRepository);
        repositories.add(deviceInfoRepository);
    }

    public void updateData() {
        for (AbstractRepository repo : repositories) {
            repo.updateData();
        }
    }

    public void updateDataOnly() {
        jobRepository.updateData();
    }

    public void updateConfiguration() {
        for (AbstractRepository repo : repositories) {
            repo.updateConfig();
        }
    }

    public void requestConfigUpdate() {
        configUpdateRequested = true;
    }

    public void resetConfiguration() {
        initialized = true;
    }

    public void updateFromBackground() {
        if (!configUpdateRequested) {
            DeviceConfig deviceConfig = configRepository.updateDeviceConfig().blockingGet();
            if (deviceConfig.isReconfigure()) {
                configUpdateRequested = true;
            }
        }
        if (configUpdateRequested) {
            updateConfiguration();
        }
        updateData();

//        //ALT!
//        if(initialized){
//            updateConfiguration();
//        }
//
//        updateData();
//
//        if (configUpdateRequested) {
//            updateConfiguration();
//            updateData();
//        }
    }
}
