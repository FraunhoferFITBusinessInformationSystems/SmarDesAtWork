/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.databinding.ObservableArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.misc.list.ArrayListChangedListener;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.common.util.ListUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.JobEntryDto;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListContextInfo;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListOverview;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStep;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepState;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TodoListRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ToggleableEntryComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableDataHelper;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import de.vogler_engineering.smartdevicesapp.viewelements.dialog.DialogButtonOptions;
import de.vogler_engineering.smartdevicesapp.viewelements.dialog.SimpleOkCancelDialogBuilder;
import de.vogler_engineering.smartdevicesapp.viewelements.util.UiMessageUtil;
import de.vogler_engineering.smartdevicesapp.viewelements.viewmodel.BasicConfigurableViewModelFeaturesImpl;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import lombok.experimental.var;
import timber.log.Timber;

public class TodoListLayoutViewModel extends AbstractLayoutViewModel {

    private static final String TAG = "TodoListLayoutViewModel";

    @Inject
    AppManager appManager;
    @Inject
    SchedulersFacade schedulersFacade;
    @Inject
    JobRepository jobRepository;
    @Inject
    ComponentFactory componentFactory;
    @Inject
    UiMessageUtil uiMessageUtil;
    @Inject
    TodoListRepository todoListRepository;

    private BasicConfigurableViewModelFeaturesImpl mFeatures;

    private ConfigurableDataHelper helper;

    private ArrayList<ToggleableEntryComponentData> componentData = new ArrayList<>();

    private TodoListChangedListener todoListChangedListener = new TodoListChangedListener();

    private JobEntryDto jobEntry = null;
    private String listId = null;
    private UUID instanceId = null;
    private String contextDomain = null;

    @Inject
    public TodoListLayoutViewModel(ViewModelReference viewModelDelegate) {
        super(viewModelDelegate);

        steps.addOnListChangedCallback(todoListChangedListener);
    }

    @Override
    public Single<Boolean> initializeAsync(JobEntryDto jobEntry) {
        if (jobEntry == null) {
            throw new IllegalArgumentException("JobEntry not set!");
        }
        UiLayout layout = jobEntry.getUi();
        if (layout == null) {
            throw new IllegalArgumentException("Ui-Layout not defined!");
        }
        this.jobEntry = jobEntry;
        mFeatures = new BasicConfigurableViewModelFeaturesImpl(appManager, schedulersFacade, uiMessageUtil, jobRepository, this);
        mFeatures.setJobEntry(jobEntry);
        helper = new ConfigurableDataHelper(layout, mFeatures, componentFactory);
        helper.initData(jobEntry.getEntry());

        return loadDataAsync();
    }

    private Single<Boolean> loadDataAsync(){
        //Load Data
        listId = null;
        instanceId = null;
        contextDomain = null;
        Map<String, String> resource = jobEntry.getEntry().getResource();
        if (resource.containsKey("list_id")) {
            listId = resource.get("list_id");
        }
        if (resource.containsKey("instance_id")) {
            String inst = resource.get("instance_id");
            try {
                instanceId = UUID.fromString(inst);
            } catch (Exception ignored) {
            }
        }
        if (resource.containsKey("domain")) {
            contextDomain = resource.get("domain");
        }
        if (listId == null || instanceId == null || contextDomain == null) {
            throw new IllegalArgumentException("list_id, instance_id or domain not set! Cannot initialize TodoList!");
        }

        return todoListRepository.getListOverview(listId, instanceId, contextDomain)
                .observeOn(schedulersFacade.ui())
                .map(x -> {
                    updateValues(x);
                    return true;
                });
    }

    @Override
    public ConfigurableDataHelper getDataHelper() {
        return helper;
    }

    @Override
    public void updateData() {
        if(jobEntry != null) {
            addDisposable(loadDataAsync()
                    .subscribeOn(schedulersFacade.newThread())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            (b) -> {},
                            (err) -> Timber.tag(TAG).e(err)
                    ));
        }
    }

    //Data Handling

    private void updateValues(TodoListOverview over) {
        for (TodoListStep step : over.getSteps()) {
            step.setType(ComponentType.ToggleableEntry);
        }

        overview.postValue(over);
        head.postValue(over.getName());
        subhead.postValue(over.getDescription());
        ListUtils.mergeInto(over.getSteps(), steps);
        contextInfo.postValue(over.getContext());
    }

    private final MutableLiveData<TodoListOverview> overview = new MutableLiveData<>();
    private final MutableLiveData<String> head = new MutableLiveData<>();
    private final MutableLiveData<String> subhead = new MutableLiveData<>();
    private final MutableLiveData<Boolean> closeEnabled = new MutableLiveData<>();
    private final MutableLiveData<TodoListContextInfo> contextInfo = new MutableLiveData<>();
    private ObservableArrayList<TodoListStep> steps = new ObservableArrayList<>();

    public LiveData<String> getHeadObservable() {
        return head;
    }

    public String getHead() {
        return head.getValue();
    }

    public LiveData<String> getSubheadObservable() {
        return subhead;
    }

    public String getSubhead() {
        return subhead.getValue();
    }

    public LiveData<Boolean> getCloseEnabledObservable() {
        return closeEnabled;
    }

    public boolean isCloseEnabled(){
        return closeEnabled.getValue() == null ? false : closeEnabled.getValue();
    }

    public LiveData<TodoListContextInfo> getContextInfoObservable() {
        return contextInfo;
    }

    public TodoListContextInfo getContextInfo() {
        return contextInfo.getValue();
    }

    public ObservableArrayList<TodoListStep> getSteps() {
        return steps;
    }

    public ComponentData getComponentData(int id) {
        return componentData.get(id);
    }

    private static UiComponent createComponent(TodoListStep step){
        UiComponent comp = new UiComponent();
        comp.setId(String.valueOf(step.getNumber()));
        comp.setName("List item");
        comp.setType(ComponentType.ToggleableEntry);
        return comp;
    }

    private static ToggleableEntryComponentData createComponentData(TodoListStep step, ConfigurableViewModelFeatures features){
        ToggleableEntryComponentData compData = new ToggleableEntryComponentData(createComponent(step), features);
        compData.setValue(step);
        return compData;
    }

    private void checkCloseEnabled(){
        boolean allChecked = true;
        for (TodoListStep step : steps) {
            if(step.getState() != TodoListStepState.Finished){
                allChecked = false;
                break;
            }
        }
        closeEnabled.postValue(allChecked);
    }

    private Map<String, String> gatherResources(String action){
        Map<String, String> resources = new HashMap<>();
        resources.put("instance_id", instanceId.toString());
        resources.put("list_id", listId);
        resources.put("domain", contextDomain);
        resources.put("action", action);
        return resources;
    }

    public void onCloseListClick(Context context) {
        if(instanceId != null && isCloseEnabled()){
            if(dialogDisposable != null && !dialogDisposable.isDisposed()){
                Timber.tag(TAG).e("Dialog not disposed! Could not start new Dialog.");
                return;
            }
            dialogDisposable = Single.fromCallable(() -> new SimpleOkCancelDialogBuilder(context))
                    .subscribeOn(schedulersFacade.newThread())
                    .observeOn(schedulersFacade.ui())
                    .map(x -> {
                        x.setupDialog(
                                this::onCloseDialogResult,
                                R.drawable.ic_assignment_turned_in_black_24dp,
                                R.string.view_todo_list_close_dialog_title,
                                R.string.view_todo_list_close_dialog_text,
                                DialogButtonOptions.YesNo);
                        return x;
                    })
                    .subscribe(
                            AlertDialog.Builder::show,
                            (err) -> Timber.tag(TAG).e(err, "Could not create Abort Dialog!")
                    );
        }
    }

    private Disposable dialogDisposable;

    public void onAbortListClick(Context context) {
        if(dialogDisposable != null && !dialogDisposable.isDisposed()){
            Timber.tag(TAG).e("Dialog not disposed! Could not start new Dialog.");
            return;
        }
        dialogDisposable = Single.fromCallable(() -> new SimpleOkCancelDialogBuilder(context))
                .subscribeOn(schedulersFacade.newThread())
                .observeOn(schedulersFacade.ui())
                .map(x -> {
                    x.setupDialog(
                            this::onAbortDialogResult,
                            R.drawable.ic_warning_black_24dp,
                            R.string.view_todo_list_abort_dialog_title,
                            R.string.view_todo_list_abort_dialog_text,
                            DialogButtonOptions.YesNo);
                    return x;
                })
                .subscribe(
                        AlertDialog.Builder::show,
                        (err) -> Timber.tag(TAG).e(err, "Could not create Abort Dialog!")
                );
    }

    public void onForwardListClick(Context context) {
        if(dialogDisposable != null && !dialogDisposable.isDisposed()){
            Timber.tag(TAG).e("Dialog not disposed! Could not start new Dialog.");
            return;
        }
        dialogDisposable = Single.fromCallable(() -> new SimpleOkCancelDialogBuilder(context))
                .subscribeOn(schedulersFacade.newThread())
                .observeOn(schedulersFacade.ui())
                .map(x -> {
                    x.setupDialog(
                            this::onForwardDialogResult,
                            R.drawable.ic_call_made_white_24dp,
                            R.string.view_todo_list_forward_dialog_title,
                            R.string.view_todo_list_forward_dialog_text,
                            DialogButtonOptions.YesNo);
                    return x;
                })
                .subscribe(
                        AlertDialog.Builder::show,
                        (err) -> Timber.tag(TAG).e(err, "Could not create Abort Dialog!")
                );
    }


    private void onCloseDialogResult(String choice) {
        if(choice.equals(SimpleOkCancelDialogBuilder.ButtonChoice.YES)){
            Map<String, String> resources = gatherResources("close");
            mFeatures.sendJob(resources);
        }
    }

    private void onAbortDialogResult(String choice) {
        if(choice.equals(SimpleOkCancelDialogBuilder.ButtonChoice.YES)){
            Map<String, String> resources = gatherResources("abort");
            mFeatures.sendJob(resources);
        }
    }

    private void onForwardDialogResult(String choice) {
        if(choice.equals(SimpleOkCancelDialogBuilder.ButtonChoice.YES)){
            Map<String, String> resources = gatherResources("forward");
            mFeatures.sendJob(resources);
        }
    }

    public String getContextDomain(){
        return contextDomain;
    }



    public class TodoListChangedListener extends ArrayListChangedListener<TodoListStep> {

        public TodoListChangedListener() {
        }

        @Override
        public void onItemRangeChanged(ObservableArrayList<TodoListStep> sender, int positionStart, int itemCount) {
            for (int i = 0; i < itemCount; i++){
                final int pos = positionStart + i;
                final TodoListStep step = steps.get(pos);

                ToggleableEntryComponentData compData = componentData.get(pos);
                if(compData != null)
                    compData.setValue(step);
                checkCloseEnabled();
            }
        }

        @Override
        public void onItemRangeInserted(ObservableArrayList<TodoListStep> sender, int positionStart, int itemCount) {
            for (int i = 0; i < itemCount; i++){
                final int pos = positionStart + i;
                final TodoListStep step = steps.get(pos);

                addDisposable(Single.fromCallable(() -> createComponentData(step, mFeatures))
                        .map(compData -> {
                            componentData.add(pos, compData);
                            return true;
                        })
                        .subscribeOn(schedulersFacade.ui())
                        .subscribe(
                                (r) -> {},
                                (err)->{}
                        ));
                checkCloseEnabled();
            }

        }

        @Override
        public void onItemRangeRemoved(ObservableArrayList<TodoListStep> sender, int positionStart, int itemCount) {
            for (int i = 0; i < itemCount; i++){
                final int pos = positionStart + i;
//                final TodoListStep step = steps.get(pos);

                addDisposable(Single.fromCallable(() -> true)
                        .map(compData -> {
                            componentData.remove(pos);
                            return true;
                        })
                        .subscribeOn(schedulersFacade.ui())
                        .subscribe(
                                (r) -> {},
                                (err)->{}
                        ));
                checkCloseEnabled();
            }
        }

        @Override
        public void onChanged(ObservableArrayList<TodoListStep> sender) {
            super.onChanged(sender);

        }

        @Override
        public void onItemRangeMoved(ObservableArrayList<TodoListStep> sender, int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(sender, fromPosition, toPosition, itemCount);
        }
    }


}
