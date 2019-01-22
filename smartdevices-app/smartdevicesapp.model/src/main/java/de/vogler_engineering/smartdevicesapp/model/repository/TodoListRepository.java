/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListClosedStep;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListDetails;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListHeader;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListInstanceDto;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListInstanceHeaderDto;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListOverview;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStep;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepDetails;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepState;
import de.vogler_engineering.smartdevicesapp.model.management.AppManager;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.RestServiceProvider;
import de.vogler_engineering.smartdevicesapp.model.services.retrofit.rest.TodoListRestService;
import io.reactivex.Single;

public class TodoListRepository extends AbstractRepository{

    private static final String TAG = "ResourceRepository";

    private final AppManager appManager;
    private final SchedulersFacade schedulersFacade;
    private final RestServiceProvider restServiceProvider;

    public TodoListRepository(AppManager appManager, SchedulersFacade schedulersFacade, RestServiceProvider restServiceProvider) {
        super(appManager, schedulersFacade);
        this.appManager = appManager;
        this.schedulersFacade = schedulersFacade;
        this.restServiceProvider = restServiceProvider;
    }

    private TodoListRestService getRestService(){
        return restServiceProvider.createRestService(TodoListRestService.class);
    }

    public Single<List<TodoListHeader>> getAllListDetails(String context){
        TodoListRestService restService = getRestService();
        return restService.getAllListDetails(context)
                .subscribeOn(schedulersFacade.newThread());
    }
    public Single<TodoListDetails> getListDetails(String key, String context){
        TodoListRestService restService = getRestService();
        return restService.getListDetails(key, context)
                .subscribeOn(schedulersFacade.newThread());
    }
    public Single<List<TodoListInstanceHeaderDto>> getAllInstances(String context){
        TodoListRestService restService = getRestService();
        return restService.getAllInstances(context)
                .subscribeOn(schedulersFacade.newThread());
    }
    public Single<TodoListInstanceDto> getInstance(UUID id){
        TodoListRestService restService = getRestService();
        return restService.getInstance(id)
                .subscribeOn(schedulersFacade.newThread());
    }
    public Single<UUID> startInstance(String listKey, String context){
        TodoListRestService restService = getRestService();
        return restService.startInstance(listKey, context, appManager.getDeviceId(), null)
                .subscribeOn(schedulersFacade.newThread());
    }
    public Single<Integer> setStepState(UUID id, int step, boolean finished){
        TodoListRestService restService = getRestService();
        return restService.confirmStep(id, step, finished, appManager.getDeviceId())
                .subscribeOn(schedulersFacade.newThread());
    }

    public Single<TodoListOverview> getListOverview(String listKey, UUID instanceId, String contextDomain){
        Single<TodoListDetails> listDetails = getListDetails(listKey, contextDomain)
                .subscribeOn(schedulersFacade.newThread());
        Single<TodoListInstanceDto> instanceDetails = getInstance(instanceId)
                .subscribeOn(schedulersFacade.newThread());

        return Single.zip(listDetails, instanceDetails,
                (details, instance) -> {
                    if(details == null || instance == null || instance.getInstance() == null){
                        throw new IllegalArgumentException("No instance found!");
                    }
                    return createListOverview(details, instance);
                });
    }

    public Single<TodoListStepDetails> getStepDetails(String listKey, UUID instanceId, String contextDomain){
        return getStepDetails(listKey, instanceId, -1, contextDomain);
    }

    public Single<TodoListStepDetails> getStepDetails(String listKey, UUID instanceId, int stepNum, String contextDomain){
        Single<TodoListDetails> listDetails = getListDetails(listKey, contextDomain)
                .subscribeOn(schedulersFacade.newThread());
        Single<TodoListInstanceDto> instanceDetails = getInstance(instanceId)
                .subscribeOn(schedulersFacade.newThread());

        return Single.zip(listDetails, instanceDetails,
                (details, instance) -> {
                    if(details == null || instance == null || instance.getInstance() == null){
                        throw new IllegalArgumentException("No instance found!");
                    }
                    if(stepNum == -1 && instance.getSteps().size() > 0){
                        return createStepDetails(details, instance, instance.getSteps().get(0).getNumber());
                    }
                    return createStepDetails(details, instance, stepNum);
                });
    }

    private static TodoListStepDetails createStepDetails(TodoListDetails details, TodoListInstanceDto instance, int currentStep){
        //Find step
        ArrayList<TodoListStep> steps = details.getSteps();
        int idx = -1;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getNumber() == currentStep) {
                idx = i;
                break;
            }
        }
        if(idx == -1){
            throw new IllegalArgumentException("Unknown step number");
        }

        TodoListStepDetails d = new TodoListStepDetails(steps.get(idx));
        d.setStepCount(steps.size());
        d.setStepIdx(idx);

        //Find next and Previous
        if(idx > 0){
            d.setPreviousStep(steps.get(idx-1));
        }
        if(idx < steps.size()-1){
            d.setNextStep(steps.get(idx+1));
        }

        d.setHeader(details.getHeader());
        d.setInstanceId(instance.getInstance().getId());

        d.setState(getStepState(instance, d.getNumber()));
        d.setClickable(d.getState() != TodoListStepState.Finished);

        if(d.getNextStep() != null){
            d.getNextStep().setState(getStepState(instance, d.getNextStep().getNumber()));
        }
        if(d.getPreviousStep() != null){
            d.getPreviousStep().setState(getStepState(instance, d.getPreviousStep().getNumber()));
        }

        return d;
    }

    private static TodoListOverview createListOverview(TodoListDetails details, TodoListInstanceDto instance){
        TodoListOverview o = new TodoListOverview(details.getHeader());
        o.setInstanceId(instance.getInstance().getId());
        ArrayList<TodoListStep> steps = details.getSteps();

        int currentStepIdx = -1;
        int currentStepNum = getCurrentStep(instance);

        for (int i = 0; i < steps.size(); i++) {
            TodoListStep step = steps.get(i);
            step.setParent(o);
            step.setState(getStepState(instance, step.getNumber()));
            step.setClickable(step.getState() != TodoListStepState.Finished);
//            steps.add(step);
            if(step.getNumber() == currentStepNum){
                currentStepIdx = i;
            }
        }

        if(currentStepIdx != -1){
            o.setCurrentStep(currentStepNum);
            if(currentStepIdx > 0){
                o.setPreviousStep(steps.get(currentStepIdx-1).getNumber());
            }
            if(currentStepIdx < steps.size()-1){
                o.setNextStep(steps.get(currentStepIdx+1).getNumber());
            }
        }
        o.setSteps(steps);

        o.setContext(instance.getInstance().getContext());
        o.getContext().setStartedAt(instance.getInstance().getStartedAt());
        o.getContext().setStartedBy(instance.getInstance().getStartedBy());

        return o;
    }

    private static TodoListStepState getStepState(Map<Integer, Boolean> states, int stepNumber){
        if(states.containsKey(stepNumber)){
            return states.get(stepNumber) ? TodoListStepState.Finished : TodoListStepState.Default;
        }
        return TodoListStepState.Default;
    }

    private static TodoListStepState getStepState(TodoListInstanceDto instance, int stepNumber){
        for (TodoListClosedStep closedStep : instance.getClosedSteps()) {
            if(closedStep.getStep() == stepNumber) return TodoListStepState.Finished;
        }
        return TodoListStepState.Default;
    }

    private static int getCurrentStep(TodoListInstanceDto instance){
        for (TodoListStep step : instance.getSteps()) {
            if(getStepState(instance, step.getNumber()) != TodoListStepState.Finished){
                return step.getNumber();
            }
        }
        return -1;
    }

    public Single<Boolean> closeTodoList(UUID uuid){
        return getRestService().closeInstance(uuid, appManager.getDeviceId());
    }
}