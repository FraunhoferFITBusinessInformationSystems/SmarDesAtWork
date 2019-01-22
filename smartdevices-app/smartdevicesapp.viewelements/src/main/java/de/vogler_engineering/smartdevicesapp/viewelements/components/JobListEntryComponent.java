/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.util.JobUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class JobListEntryComponent extends BaseComponent<Job> {


    public JobListEntryComponent() {
        super(ComponentType.JobListEntry);
    }

    private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);

    protected FrameLayout mContainer;
    protected View mLayout;

    protected TextView mTitleTextView;
    protected TextView mSubtitleTextView;
    protected TextView mDateTextView;
    protected TextView mStateTextView;

    private Observer<Job> observer = null;

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());
        mLayout = (View) inflater.inflate(R.layout.component_job_list_entry,
                mContainer, false);
        mContainer.addView(mLayout);

        mTitleTextView = mLayout.findViewById(R.id.job_list_entry_title);
        mSubtitleTextView = mLayout.findViewById(R.id.job_list_entry_subtitle);
        mDateTextView = mLayout.findViewById(R.id.job_list_entry_date);
        mStateTextView = mLayout.findViewById(R.id.job_list_entry_state);

        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData componentData, LifecycleOwner lifecycleOwner) {
        final JobListEntryComponentData data = (JobListEntryComponentData) componentData;
        //Set initially
        if(data == null){
            return;
        }
        if(data.getValue() != null) {
            onDataUpdated(data.getValue());
        }
        //Add listener

//        if(observer == null)
//            data.valueLiveData().removeObserver(observer);
//        observer = (d) -> {
//            if(d != null){
//                onDataUpdated(d);
//            }
//        };
        data.valueLiveData().observe(lifecycleOwner, (d) -> {
            if(d != null){
                onDataUpdated(d);
            }
        });

        mContainer.setOnClickListener((a) -> data.onClicked());
    }

    private void onDataUpdated(Job data){
        mTitleTextView.setText(JobUtils.getTypeName(data));
        mSubtitleTextView.setText(JobUtils.getSubtitle(data));
        if(data.getCreatedAt() != null) {
            mDateTextView.setText(dateFormat.format(data.getCreatedAt()));
        }
        mStateTextView.setText(data.getStatus().job_status_text_resource);
    }

    @Override
    public void dispose() {
        mTitleTextView = null;
        mSubtitleTextView = null;
        mDateTextView = null;
        mStateTextView = null;
        mContainer = null;
        mLayout = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}
