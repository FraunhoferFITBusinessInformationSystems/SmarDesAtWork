/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.vogler_engineering.smartdevicesapp.common.util.MapUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListContextInfo;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStep;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentDataProvider;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentFactory;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableRecyclerAdapter;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutView;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutViewModelReference;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.TodoListLayoutViewModel;

public class TodoListView extends LayoutView implements ComponentDataProvider {

    private static final String TAG = "TodoListView";

    private TextView mHeadText;
    private TextView mSubheadText;
    private RecyclerView mRecycler;
    private ConstraintLayout mContainer;
    private ComponentFactory factory;

    private Button mCloseButton;
    private Button mAbortButton;
    private Button mForwardButton;

    private LinearLayout mComponentLayout;

    private ConfigurableRecyclerAdapter<TodoListStep> mAdapter;

    private TodoListLayoutViewModel viewModel = null;
    private GridLayout mInfoGrid;

    private final UiLayout layout;

    private Boolean canDelete = false;
    private Boolean canForward = false;

    public TodoListView(UiLayout layout) {
        super();
        this.layout = layout;
    }

    public void setComponentFactory(ComponentFactory factory){
        this.factory = factory;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup parentView) {
        mContainer = (ConstraintLayout)inflater.inflate(R.layout.view_todo_list,
                parentView, false);

        mHeadText = mContainer.findViewById(R.id.view_todo_list_head);
        mSubheadText = mContainer.findViewById(R.id.view_todo_list_subhead);
        mCloseButton = mContainer.findViewById(R.id.view_todo_list_close_button);
        mAbortButton = mContainer.findViewById(R.id.view_todo_list_abort_button);
        mForwardButton = mContainer.findViewById(R.id.view_todo_list_forward_button);
        mInfoGrid = mContainer.findViewById(R.id.view_todo_list_info_grid);

        mComponentLayout = mContainer.findViewById(R.id.view_todo_list_component_layout);

        mRecycler = mContainer.findViewById(R.id.view_todo_list_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        return mContainer;
    }

    @Override
    public void bindView(Context context, LayoutViewModelReference viewModelRef, LifecycleOwner lifecycleOwner) {
        if(viewModel == null){
            if(viewModelRef instanceof TodoListLayoutViewModel){
                viewModel = (TodoListLayoutViewModel)viewModelRef;
            }
        }

        Object obj;
        obj= MapUtils.getKeyIgnoreCapitalCase(layout.getAdditionalProperties(), "CanDelete");
        if(obj != null){
            canDelete = Boolean.valueOf(obj.toString());
        }
        obj = MapUtils.getKeyIgnoreCapitalCase(layout.getAdditionalProperties(), "CanForward");
        if(obj != null){
            canForward = Boolean.valueOf(obj.toString());
        }
        if(!canDelete){
            mAbortButton.setClickable(false);
            mAbortButton.setEnabled(false);
            mAbortButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.textDarkDisabled, null));
        }
        if(!canForward){
            mForwardButton.setClickable(false);
            mForwardButton.setEnabled(false);
            mForwardButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.textDarkDisabled, null));
        }


        if(viewModel != null){
            mHeadText.setText(viewModel.getHead());
            viewModel.getHeadObservable().observe(lifecycleOwner, (t) -> mHeadText.setText(t));
            mSubheadText.setText(viewModel.getSubhead());
            viewModel.getSubheadObservable().observe(lifecycleOwner, (t) -> mSubheadText.setText(t));
            setCloseButtonEnableState(context, viewModel.isCloseEnabled());
            viewModel.getCloseEnabledObservable().observe(lifecycleOwner, (t) ->
                    setCloseButtonEnableState(context, !(t==null || !t)));

            viewModel.getContextInfoObservable().observe(lifecycleOwner, t -> updateContextInfo(context, t));

            mCloseButton.setOnClickListener((v) -> viewModel.onCloseListClick(context));
            mAbortButton.setOnClickListener((v) -> viewModel.onAbortListClick(context));
            mForwardButton.setOnClickListener((v) -> viewModel.onForwardListClick(context));

            if(mAdapter == null) {
                mAdapter = new ConfigurableRecyclerAdapter<>(context, this, factory, lifecycleOwner);
                mAdapter.setItems(viewModel.getSteps());
            }
            mRecycler.setAdapter(mAdapter);
        }

        mRecycler.setNestedScrollingEnabled(false);
    }

    private void updateContextInfo(Context context, TodoListContextInfo info) {

//        JobViewLayoutHelper helper = new JobViewLayoutHelper(layout);
//        helper.createViews();

        mInfoGrid.removeAllViews();
        TextView tvLabel, tvValue;
        for(int i = 0; i < 5; i++){
            tvLabel = new TextView(context);
            GridLayout.LayoutParams labelParam = new GridLayout.LayoutParams();
            labelParam.setGravity(Gravity.START | Gravity.FILL_HORIZONTAL);
            labelParam.width = GridLayout.LayoutParams.WRAP_CONTENT;
            labelParam.height = GridLayout.LayoutParams.WRAP_CONTENT;
            labelParam.columnSpec = GridLayout.spec(0);
            labelParam.rowSpec = GridLayout.spec(i);
            tvLabel.setLayoutParams (labelParam);

            tvValue = new TextView(context);
            GridLayout.LayoutParams valueParam = new GridLayout.LayoutParams();
            valueParam.setGravity(Gravity.END);
            labelParam.width = GridLayout.LayoutParams.WRAP_CONTENT;
            labelParam.height = GridLayout.LayoutParams.WRAP_CONTENT;
            valueParam.columnSpec = GridLayout.spec(1);
            valueParam.rowSpec = GridLayout.spec(i);
            valueParam.leftMargin = (int)(context.getResources().getDisplayMetrics().density * 15);
            tvValue.setLayoutParams (valueParam);

            //TODO Outsource this String Labels to a config (preferable to the TodoListJob Config)
            // ==> Maybe use LayoutHelper or UiLayout from config
            switch (i){
                case 0: //number
                    if(info.getNumber() == null)
                        continue;
                    tvLabel.setText("Auftragsnummer:");
                    tvValue.setText(info.getNumber());
                    break;
                case 1: //subject
                    if(info.getSubject() == null)
                        continue;
                    tvLabel.setText("Maschine:");
                    tvValue.setText(info.getSubject());
                    break;
                case 2: //startedAt
                    if(info.getStartedAt() == null)
                        continue;
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
                    tvLabel.setText("Gestartet um:");
                    tvValue.setText(df.format(info.getStartedAt()));
                    break;
                case 3: //startedBy
                    if(info.getStartedBy() == null)
                        continue;
                    tvLabel.setText("Gestartet von:");
                    tvValue.setText(info.getStartedBy());
                    break;
                case 4: //notes
                    if(info.getNotes() == null)
                        continue;
                    tvLabel.setText("Notizen:");
                    tvValue.setText(info.getNotes());
                    break;
            }
            mInfoGrid.addView(tvLabel);
            mInfoGrid.addView(tvValue);


        }





    }

    private void setCloseButtonEnableState(Context context, boolean enabled){
        mCloseButton.setEnabled(enabled);
        mCloseButton.setBackgroundTintList(context.getResources().getColorStateList(enabled?R.color.colorPrimary:R.color.textDarkDisabled, null));
//        mCloseButton.setTextColor(context.getColor(enabled?R.color.material_light_white:R.color.material_grey_500));
    }

    @Override
    public void dispose() {
        mHeadText = null;
        mSubheadText = null;
        mContainer = null;
        super.dispose();
    }

    @Override
    public View getView() {
        return mContainer;
    }

    @Override
    public ComponentData getComponentData(int id) {
        return viewModel.getComponentData(id);
    }
}
