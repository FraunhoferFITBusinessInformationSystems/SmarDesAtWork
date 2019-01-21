/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist.TodoListView;

public class TodoListLayoutHelper implements LayoutHelper {

    private final UiLayout layout;

    TodoListView mView;

    @Inject
    ComponentFactory componentFactory;

    public TodoListLayoutHelper(UiLayout layout) {
        this.layout = layout;
    }

    @Override
    public void createViews(Context context, LayoutInflater inflater, ViewGroup parentView, LifecycleOwner owner) {
        mView = componentFactory.createTodoListView(layout);
        mView.setLifecycleOwner(owner);
        mView.setComponentFactory(componentFactory);
        View view = mView.createView(context, inflater, parentView);
        parentView.addView(view);
    }

    @Override
    public void bindViews(Context context, LayoutViewModelReference viewModel, LifecycleOwner owner) {
        mView.bindView(context, viewModel, owner);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mView.onActivityResult(requestCode, resultCode, data);
    }
}
