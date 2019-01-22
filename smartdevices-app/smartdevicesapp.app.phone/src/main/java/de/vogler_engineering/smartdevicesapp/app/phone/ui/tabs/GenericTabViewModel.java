/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.misc.list.ArrayListChangedListener;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.TabEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.entities.message.MessageReply;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.management.Navigator;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.MessageRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentDataProvider;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableAdapterViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.viewmodel.BasicConfigurableViewModelFeatures;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class GenericTabViewModel extends ComponentProviderViewModel {

    private static final String TAG = "GenericTabViewModel";

    private final Navigator navigationController;
    private final JobRepository jobRepository;
    private final ComponentFactory componentFactory;
    private SchedulersFacade schedulersFacade;

    private final MessageRepository messageRepository;

    private final ConfigurableAdapterViewModel<Job, UiComponent> mAdapterViewModel;
    private final TabEntryListChangedListener mTabEntryListChangedListener;

    private String tabKey = null;

    @Inject
    public GenericTabViewModel(Navigator navigator, JobRepository jobRepository,
                               ComponentFactory componentFactory, SchedulersFacade schedulersFacade, MessageRepository messageRepository) {
        this.navigationController = navigator;
        this.jobRepository = jobRepository;
        this.componentFactory = componentFactory;
        this.schedulersFacade = schedulersFacade;
        this.messageRepository = messageRepository;


        ListViewModelFeatures mFeatures = new ListViewModelFeatures();
        mAdapterViewModel = new ConfigurableAdapterViewModel<>(mFeatures, this.componentFactory);

        mTabEntryListChangedListener = new TabEntryListChangedListener(this);
    }

    public void setTabKey(String tabKey) {
        this.tabKey = tabKey;
        rebindListener();
    }

    private void rebindListener() {
        if (tabKey != null) {
            mAdapterViewModel.clearAll();

            ObservableArrayList<TabEntryDto> entries = jobRepository.getEntries(tabKey);
            for (int i = 0; i < entries.size(); i++) {
                TabEntryDto entry = entries.get(i);
                mAdapterViewModel.addComponent(i, getUi(entry), entry.getEntry());
            }
            entries.addOnListChangedCallback(mTabEntryListChangedListener);
        }
    }

    private UiComponent createDefaultUi(Job job) {
        UiComponent jobEntryComp = new UiComponent();
        jobEntryComp.setId(job.getId().toString());
        jobEntryComp.setType(ComponentType.JobListEntry);
        jobEntryComp.setName(job.getName());
        return jobEntryComp;
    }

    private UiComponent getUi(TabEntryDto entry) {
        UiComponent ui = entry.getListUi();
        if (ui == null) {
            ui = createDefaultUi(entry.getEntry());
            Timber.tag(TAG).w("No JobEntryUi provided, using Default UI for %s", entry.getEntry().getId());
        }
        return ui;
    }

    @Override
    public ComponentDataProvider getComponentDataProvider() {
        return mAdapterViewModel;
    }

    @Override
    public ObservableList<UiComponent> getItems() {
        return mAdapterViewModel.getComponents();
    }

    @Override
    public ComponentData getComponentData(int id) {
        return mAdapterViewModel.getComponentData(id);
    }

    private class TabEntryListChangedListener extends ArrayListChangedListener<TabEntryDto> {

        private GenericTabViewModel reference;
        final CompositeDisposable disposables = new CompositeDisposable();

        TabEntryListChangedListener(GenericTabViewModel genericTabViewModel) {
            this.reference = genericTabViewModel;
        }

        @Override
        public void onItemRangeInserted(ObservableArrayList<TabEntryDto> sender, int positionStart, int itemCount) {
            disposables.add(Single.fromCallable(() -> {
                for (int i = 0; i < itemCount; i++) {
                    TabEntryDto entry = sender.get(i + positionStart);
                    reference.mAdapterViewModel.addComponent(i, getUi(entry), entry.getEntry());
                }
                return true;
            }).subscribeOn(schedulersFacade.ui())
            .subscribe());
        }

        @Override
        public void onItemRangeChanged(ObservableArrayList<TabEntryDto> sender, int positionStart, int itemCount) {
            disposables.add(Single.fromCallable(() -> {
                for (int i = 0; i < itemCount; i++) {
                    TabEntryDto entry = sender.get(i + positionStart);

                    reference.mAdapterViewModel.updateData(i + positionStart, entry.getEntry());
                    if (entry.getListUi() != null) {

                        if (reference.mAdapterViewModel.getComponents().size() > i + positionStart &&
                                !entry.getListUi().equals(reference.mAdapterViewModel.getComponent(i + positionStart))) {
                            reference.mAdapterViewModel.updateComponent(i + positionStart, getUi(entry));
                        }
                    }
                }
                return true;
            }).subscribeOn(schedulersFacade.ui())
                    .subscribe());

        }

        @Override
        public void onItemRangeRemoved(ObservableArrayList<TabEntryDto> sender, int positionStart, int itemCount) {
            disposables.add(Single.fromCallable(() -> {
                for (int i = 0; i < itemCount; i++) {
                    reference.mAdapterViewModel.removeComponent(i + positionStart);
                }
                return true;
            }).subscribeOn(schedulersFacade.ui())
                    .subscribe());
        }
    }

    private class ListViewModelFeatures extends BasicConfigurableViewModelFeatures {

        @Override
        public void openJob(String jobKey, UUID id) {
            navigationController.navigateToJob(jobKey, id);
        }

        @Override
        public void startJob(String jobKey) {
            Map<String, Object> props = new HashMap<>();
            MessageReply msg = messageRepository.createReplyMessage("StartJob", jobKey, props);

            navigationController.navigateToJob(jobKey, msg.getId());
            disposables.add(messageRepository.sendMessageReply(msg)
                    .observeOn(schedulersFacade.io())
                    .subscribe(
                            (b) -> {
                                if (b) {
                                    Timber.tag(TAG).i("MessageReply sent!");
                                }
                            },
                            (e) -> Timber.tag(TAG).e(e, "Error while sending MessageReply")
                    ));
        }
    }
}
