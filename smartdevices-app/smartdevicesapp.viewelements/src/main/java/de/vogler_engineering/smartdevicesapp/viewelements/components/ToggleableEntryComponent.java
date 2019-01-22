/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStep;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepState;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class ToggleableEntryComponent extends BaseComponent<TodoListStep> {

    protected FrameLayout mContainer;
    protected View mLayout;

    private TextView mNumberView;
    private TextView mTextView;
    private ImageView mImageView;

    public ToggleableEntryComponent() {
        super(ComponentType.ToggleableEntry);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());
        mLayout = inflater.inflate(R.layout.component_toggleable_entry,
                mContainer, false);
        mContainer.addView(mLayout);

        mNumberView = mLayout.findViewById(R.id.component_toggleable_entry_num);
        mTextView = mLayout.findViewById(R.id.component_toggleable_entry_text);
        mImageView = mLayout.findViewById(R.id.component_toggleable_entry_icon);

//        mCheckBoxView.setText(this.element.getName());
//        mNumberView.setText(null);

        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData<TodoListStep> componentData, LifecycleOwner lifecycleOwner) {
        ToggleableEntryComponentData data = (ToggleableEntryComponentData) componentData;
        TodoListStep value = data.getValue();
        if (value != null) {
            mNumberView.setText(String.valueOf(value.getNumber()));
            mTextView.setText(value.getName());
        }

        data.valueLiveData().observe(lifecycleOwner, (val) -> {
            if (val != null) {
                mNumberView.setText(String.valueOf(val.getNumber()));
                mTextView.setText(val.getName());
            }
        });

        mLayout.setClickable(!data.isFinished());
        data.getFinishedObservable().observe(lifecycleOwner, (b) -> {
            mLayout.setClickable(b == null || !b);
        });

        data.valueLiveData().observe(lifecycleOwner, (val) -> {
            if (val != null) {
                TodoListStepState state = val.getState();
                switch (state) {
                    case Active:
                    case Default:
                        mImageView.setImageDrawable(context.getDrawable(R.drawable.ic_circle_circle_outline_gray_24dp));
                        return;
                    case Finished:
                        mImageView.setImageDrawable(context.getDrawable(R.drawable.ic_check_circle_green_24dp));
                        return;
                }

            }
        });

        mLayout.setOnClickListener((l) -> data.onClicked());
    }

    @Override
    public void dispose() {
        mNumberView = null;
        mTextView = null;
        mLayout = null;
        mContainer = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}
