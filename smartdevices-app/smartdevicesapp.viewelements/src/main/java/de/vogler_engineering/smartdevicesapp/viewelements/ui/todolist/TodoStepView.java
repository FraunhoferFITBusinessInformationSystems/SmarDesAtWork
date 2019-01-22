/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepDetails;
import de.vogler_engineering.smartdevicesapp.model.entities.todolist.TodoListStepState;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutView;
import de.vogler_engineering.smartdevicesapp.viewelements.layouts.LayoutViewModelReference;
import timber.log.Timber;

public class TodoStepView extends LayoutView {

    private static final String TAG = "TodoStepView";

    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mHeadText;
    private TextView mSubHeadText;
    private TextView mContentText;
    private View mContentView;
    private TextView mNumberingText;
    private Button mNavPrevButton;
    private Button mNavNextButton;

    private View mActionSetDoneLayout;
    private Button mActionSetDoneButton;
    private View mActionUndoLayout;
    private Button mActionUndoLayoutButton;

    private int mColorTextDisabled;
    private int mColorText;
//    private Button mNavFinishedNextButton;
//    private ConstraintLayout mBottomFreeNavLayout;
//    private ConstraintLayout mBottomOnlyNextNavLayout;

    private boolean initialized;

    @Inject
    ResourceRepository resourceRepository;

    public TodoStepView(Activity activity) {
        super();
//        ButterKnife.bind(activity);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup parentView) {
        mImageView = parentView.findViewById(R.id.view_card_image);
        mProgressBar = parentView.findViewById(R.id.view_card_progress);
        mHeadText = parentView.findViewById(R.id.view_card_header_text);
        mSubHeadText = parentView.findViewById(R.id.view_card_subhead_text);
        mContentText = parentView.findViewById(R.id.view_card_content_text);
        mContentView = parentView.findViewById(R.id.view_card_content_additional);
        mNumberingText = parentView.findViewById(R.id.view_card_numbering_text);
        mNavPrevButton = parentView.findViewById(R.id.view_card_button_prev);
        mNavNextButton = parentView.findViewById(R.id.view_card_button_next);


        mActionSetDoneLayout = parentView.findViewById(R.id.view_card_bottom_actions_set_done);
        mActionSetDoneButton = parentView.findViewById(R.id.view_card_bottom_actions_set_done_button);
        mActionUndoLayout = parentView.findViewById(R.id.view_card_bottom_actions_undo);
        mActionUndoLayoutButton = parentView.findViewById(R.id.view_card_bottom_actions_undo_button);


//        mNavFinishedNextButton = parentView.findViewById(R.id.view_card_button_finished_next);
//        mBottomFreeNavLayout = parentView.findViewById(R.id.view_card_bottom_free_nav_actions);
//        mBottomOnlyNextNavLayout = parentView.findViewById(R.id.view_card_bottom_only_next_actions);

        Resources r = context.getResources();


        mColorTextDisabled = r.getColor(R.color.textDarkSecondaryDisabled, null);
        mColorText = r.getColor(R.color.textDarkSecondary, null);

//        ButterKnife.bind(parentView);
        return null;
    }


    public void bindView(Context context, TodoStepViewModel viewModel, LifecycleOwner lifecycleOwner) {
        viewModel.getDetailsOverview().observe(lifecycleOwner, (details) ->
                updateData(details, context, lifecycleOwner));

        mActionSetDoneButton.setOnClickListener(v -> {
            mActionSetDoneButton.setEnabled(false);
            viewModel.checkedStatusChanged(context, true);
        });
        mActionUndoLayoutButton.setOnClickListener(v -> {
            mActionUndoLayoutButton.setEnabled(false);
            viewModel.checkedStatusChanged(context, false);
        });
        mNavNextButton.setOnClickListener((v) -> viewModel.nextCardClick(context));
        mNavPrevButton.setOnClickListener((v) -> viewModel.prevCardClick(context));

//        viewModel.getLoadingStateObservable().observe(lifecycleOwner, (als) -> {
//            if(als == ActivityLoadingState.Active){
//
//            }else if(als == ActivityLoadingState.Loading){
//                mProgressBar.setIndeterminate(true);
//                mProgressBar.setVisibility(View.VISIBLE);
//            }
//        });

        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.INVISIBLE);
        viewModel.getRequestActiveObservable().observe(lifecycleOwner, (b) ->
                mProgressBar.setVisibility((b == null || !b) ? View.INVISIBLE : View.VISIBLE));
    }

    @Override
    public void bindView(Context context, LayoutViewModelReference viewModel, LifecycleOwner lifecycleOwner) {
    }

    @Override
    public View getView() {
        return null;
    }

    public void updateData(TodoListStepDetails details, Context context, LifecycleOwner lifecycleOwner) {
        mHeadText.setText(details.getName());
        mSubHeadText.setVisibility(View.GONE);
        mContentText.setText(details.getDescription());
//        mBottomFreeNavLayout.setVisibility(View.VISIBLE);
//        mBottomOnlyNextNavLayout.setVisibility(View.GONE);

        if(details.getResource() == null){
            mImageView.setVisibility(View.GONE);
        }else{
            mImageView.setVisibility(View.VISIBLE);
            try {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int height = getFromDp(metrics, 194);
                resourceRepository.loadImage(details.getResource())
                        .resize(0, height)
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_placeholder)
                        .error(R.drawable.ic_warning_black_24dp)
                        .into(mImageView);
            }catch(ResourceRepository.ImageLoadingException e) {
                Timber.tag(TAG).e(e, "Error while Image Resource loading from [%s]", details.getResource());
            }
        }

        if(details.getNextStep() != null && details.getNextStep().getNumber() >= 0) {
            mNavNextButton.setEnabled(true);
            mNavNextButton.setTextColor(mColorText);
            mNavNextButton.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,  context.getDrawable(R.drawable.ic_navigate_next_dark_24dp),null);
        }else{
            mNavNextButton.setEnabled(false);
            mNavNextButton.setTextColor(mColorTextDisabled);
            mNavNextButton.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, context.getDrawable(R.drawable.ic_navigate_next_darkdisabled_24dp),null);
        }

        if(details.getPreviousStep() != null && details.getPreviousStep().getNumber() >= 0) {
            mNavPrevButton.setEnabled(true);
            mNavPrevButton.setTextColor(mColorText);
            mNavPrevButton.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(R.drawable.ic_navigate_before_dark_24dp),null, null, null);
        }else{
            mNavPrevButton.setEnabled(false);
            mNavPrevButton.setTextColor(mColorTextDisabled);
            mNavPrevButton.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(R.drawable.ic_navigate_before_darkdisabled_24dp),null, null, null);
        }

        if(details.getState() == TodoListStepState.Default){
            mActionUndoLayout.setVisibility(View.GONE);
            mActionSetDoneLayout.setVisibility(View.VISIBLE);
            mActionSetDoneButton.setEnabled(true);
//            mCheckedButton.setCompoundDrawablesWithIntrinsicBounds(
//                    context.getDrawable(R.drawable.ic_check_circle_gray_24dp),null, null, null);
//            mCheckedButton.setText(R.string.view_todo_card_not_finished_text);
        }else{
            mActionUndoLayout.setVisibility(View.VISIBLE);
            mActionSetDoneLayout.setVisibility(View.GONE);
            mActionUndoLayoutButton.setEnabled(true);
//            mCheckedButton.setCompoundDrawablesWithIntrinsicBounds(
//                    context.getDrawable(R.drawable.ic_check_circle_green_24dp),null, null, null);
//            mCheckedButton.setText(R.string.view_todo_card_finished_text);
        }

        updateStepNumbering(context, details.getStepCount(), details.getStepIdx()+1);
    }

    private void updateStepNumbering(Context context, int stepCount, int currentStep){
        mNumberingText.setText(context.getResources().getString(R.string.view_todo_card_numbering, currentStep, stepCount));
    }
}
