/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractViewModel;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AbstractDetailViewModel extends AbstractViewModel implements ViewModelReference {

    private static final String TAG = "AbstractDetailViewModel";

    private final SchedulersFacade schedulersFacade;
    private final JobRepository jobRepository;
    private final LayoutHelpers layoutHelpers;

    protected MutableLiveData<ActivityLoadingState> mLoadingState = new MutableLiveData<>();
    private MutableLiveData<Void> mLoadingFinishedObservable = new MutableLiveData<>();
    private MutableLiveData<String> mErrorText = new MutableLiveData<>();
    private JobEntryDto jobEntry;
    private AbstractLayoutViewModel layoutViewModel;

    protected AbstractDetailViewModel(SchedulersFacade schedulersFacade, JobRepository jobRepository, LayoutHelpers layoutHelpers) {
        this.schedulersFacade = schedulersFacade;
        this.jobRepository = jobRepository;
        this.layoutHelpers = layoutHelpers;
    }

    public void loadJobAsync(UUID jobId) {
        disposables.add(jobRepository.getJob(jobId, 250)
                .subscribeOn(schedulersFacade.ui())
                .subscribe((jobEntryDto) -> {
                            this.jobEntry = jobEntryDto;
                            try {
                                initSpecificViewModel(jobEntry);
                            } catch (Exception e) {
                                postError(e);
                                Timber.tag(TAG).e(e, "Error while initializing");
                            }
                        },
                        (err) -> {
                            postError(err);
                            Timber.tag(TAG).e(err, "Could not get Job from Server");
                        }));
    }

    protected void initSpecificViewModel(JobEntryDto jobEntry) throws Exception {
        if (jobEntry == null || jobEntry.getEntry() == null || jobEntry.getUi() == null) {
            throw new Exception("Could not load Job: Incorrect data!");
        }
        String type = jobEntry.getUi().getType();
        if (type == null) {
            throw new Exception("Could not load Job: UiType is not set!");
        }
        layoutViewModel = layoutHelpers.createLayoutViewModel(jobEntry.getUi(), this);

        disposables.add(layoutViewModel.initializeAsync(jobEntry)
                .subscribeOn(schedulersFacade.newThread())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        b -> {
                            Timber.tag(TAG).i("Successfully initialized DetailViewModel");

                            //Notify activity that loading is finished.
                            mLoadingFinishedObservable.postValue(null);
                        },
                        (err) -> {
                            postError(err);
                            Timber.tag(TAG).e(err, "Error while initializing DetailViewModel");
                        }
                ));
    }

    protected void setJobEntry(JobEntryDto jobEntry) {
        this.jobEntry = jobEntry;
    }

    protected JobEntryDto getJobEntry() {
        return jobEntry;
    }

    public AbstractLayoutViewModel getLayoutViewModel() {
        return layoutViewModel;
    }

    public MutableLiveData<Void> getLoadingFinishedObservable() {
        return mLoadingFinishedObservable;
    }

    public LiveData<String> getErrorTextObservable() {
        return mErrorText;
    }

    public void postError(String errorText){
        mErrorText.postValue(errorText);
        mLoadingState.postValue(ActivityLoadingState.Error);
    }

    public void postError(Throwable throwable){
        postError(throwable.toString());
    }

    @Override
    public void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void postLoadingState(ActivityLoadingState state) {
        mLoadingState.postValue(state);
    }

    @Override
    public ActivityLoadingState getLoadingState() {
        return mLoadingState.getValue();
    }

    @Override
    public void finishActivity() {
        super.finishActivity();
    }

    public MutableLiveData<ActivityLoadingState> getLoadingStateObservable() {
        return mLoadingState;
    }

    @Override
    public void updateData() {
    }

    public void onBackPressed() {
        if(layoutViewModel != null){
            layoutViewModel.onBackPressed();
        }
    }
}
