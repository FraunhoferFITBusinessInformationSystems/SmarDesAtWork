/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import javax.inject.Inject;

import dagger.MembersInjector;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;

public class LayoutHelpers {

    @Inject MembersInjector<TodoListLayoutHelper>       todoListLayoutHelperInjector;
    @Inject MembersInjector<TodoListLayoutViewModel>    todoListLayoutViewModelInjector;

    @Inject MembersInjector<JobViewLayoutHelper>        jobViewLayoutHelperInjector;
    @Inject MembersInjector<JobViewLayoutViewModel>     jobViewLayoutViewModelInjector;

    @Inject
    public LayoutHelpers() {
    }

    public LayoutHelper createLayoutHelper(UiLayout layout) throws UnknownLayoutHelperException {
        if(layout == null || layout.getType() == null) {
            throw new UnknownLayoutHelperException("LayoutType is not set!");
        }
        String type = layout.getType().toLowerCase();
        switch (type){
            case "jobview":
                JobViewLayoutHelper jobViewLayoutHelper = new JobViewLayoutHelper(layout);
                jobViewLayoutHelperInjector.injectMembers(jobViewLayoutHelper);
                return jobViewLayoutHelper;
            case "todolist":
                TodoListLayoutHelper todoListLayoutHelper = new TodoListLayoutHelper(layout);
                todoListLayoutHelperInjector.injectMembers(todoListLayoutHelper);
                return todoListLayoutHelper;
        }
        throw new UnknownLayoutHelperException(String.format("Unknown LayoutType specified! \"%s\"", layout.getType()));
    }

    public AbstractLayoutViewModel createLayoutViewModel(UiLayout layout, ViewModelReference viewModelDelegate) throws UnknownLayoutHelperException {
        String type = layout.getType();
        if(type == null) {
            throw new UnknownLayoutHelperException("LayoutType is not set!");
        }
        type = type.toLowerCase();
        switch (type){
            case "jobview":
                JobViewLayoutViewModel jobViewLayoutViewModel = new JobViewLayoutViewModel(viewModelDelegate);
                jobViewLayoutViewModelInjector.injectMembers(jobViewLayoutViewModel);
                return jobViewLayoutViewModel;
            case "todolist":
                TodoListLayoutViewModel todoListLayoutViewModel = new TodoListLayoutViewModel(viewModelDelegate);
                todoListLayoutViewModelInjector.injectMembers(todoListLayoutViewModel);
                return todoListLayoutViewModel;
        }
        throw new UnknownLayoutHelperException(String.format("Unknown LayoutType specified! \"%s\"", layout.getType()));
    }

    public static class UnknownLayoutHelperException extends Exception {
        public UnknownLayoutHelperException(String message) {
            super(message);
        }
    }
}
