/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.common.util.MapUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableDataHelper;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import de.vogler_engineering.smartdevicesapp.viewelements.viewmodel.BasicConfigurableViewModelFeaturesImpl;
import io.reactivex.Single;

public class JobViewLayoutViewModel extends AbstractLayoutViewModel {

    private static final String TAG = "JobViewLayoutViewModel";

    @Inject AppManager appManager;
    @Inject SchedulersFacade schedulersFacade;
    @Inject JobRepository jobRepository;
    @Inject ComponentFactory componentFactory;
    @Inject UiMessageUtil uiMessageUtil;
    private BasicConfigurableViewModelFeaturesImpl mFeatures;

    private UiLayout layout;
    private ConfigurableDataHelper helper;

    public JobViewLayoutViewModel(ViewModelReference viewModelDelegate) {
        super(viewModelDelegate);
    }

    @Override
    public Single<Boolean> initializeAsync(JobEntryDto jobEntry) {
        if(jobEntry == null){
            throw new IllegalArgumentException("JobEntry not set!");
        }
        layout = jobEntry.getUi();
        if(layout == null){
            throw new IllegalArgumentException("Ui-Layout not defined!");
        }

        mFeatures = new BasicConfigurableViewModelFeaturesImpl(appManager, schedulersFacade, uiMessageUtil, jobRepository, this);
        mFeatures.setJobEntry(jobEntry);
        helper = new ConfigurableDataHelper(layout, mFeatures, componentFactory);
        helper.initData(jobEntry.getEntry());

        return Single.just(true);
    }

    @Override
    public ConfigurableDataHelper getDataHelper() {
        return helper;
    }

    @Override
    public void onBackPressed() {
        if(layout != null && layout.getAdditionalProperties() != null){
            Object onBackPressed = MapUtils.getKeyIgnoreCapitalCase(layout.getAdditionalProperties(), "OnBackPressed");
            if(onBackPressed != null){
                String action = String.class.cast(onBackPressed);
                if (action != null) {
                    switch (action) {
                        case "RemoveJob":
                            mFeatures.removeJob();
                            break;
                    }
                }
            }
        }
    }
}
