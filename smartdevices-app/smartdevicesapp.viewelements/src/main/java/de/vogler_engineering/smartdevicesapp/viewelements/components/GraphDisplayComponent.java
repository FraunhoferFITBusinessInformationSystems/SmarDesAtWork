/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.value.GenericValueData;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.BaseComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;

public class GraphDisplayComponent extends BaseComponent<GenericValueData> {

    protected FrameLayout mContainer;
    protected GridLayout mGridLayout;

    protected TextView mDisplayNameTextView;
    protected TextView mNameTextView;
    protected TextView mValueNominalTextView;
    protected TextView mValueActualTextView;
    protected TextView mStatusTextView;
    private GraphView mGraphView;

    protected String mPrefix = "";
    protected String mPostfix = "";

    private int COLOR_BG_NORMAL = 0;
    private int COLOR_BG_WARNING = 0;
    private int COLOR_BG_INFO = 0;
    private int COLOR_BG_ERROR = 0;

    public GraphDisplayComponent() {
        super(ComponentType.GraphDisplay);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        COLOR_BG_NORMAL = context.getResources().getColor(R.color.value_display_background_normal, context.getTheme());
        COLOR_BG_WARNING = context.getResources().getColor(R.color.value_display_background_warning, context.getTheme());
        COLOR_BG_INFO = context.getResources().getColor(R.color.value_display_background_info, context.getTheme());
        COLOR_BG_ERROR = context.getResources().getColor(R.color.value_display_background_error, context.getTheme());

        //Setup Layout
        mContainer = new FrameLayout(context);
        mContainer.setLayoutParams(getDefaultLayoutParams());

        mGridLayout = (GridLayout) inflater.inflate(R.layout.component_graph_display,
                mContainer, false);

        mDisplayNameTextView = mGridLayout.findViewById(R.id.comp_graph_display_display_name);
        mNameTextView = mGridLayout.findViewById(R.id.comp_graph_display_name);
        mValueNominalTextView = mGridLayout.findViewById(R.id.comp_graph_display_nominal_value);
        mValueActualTextView = mGridLayout.findViewById(R.id.comp_graph_display_actual_value);
        mStatusTextView = mGridLayout.findViewById(R.id.comp_graph_display_status_text);
        mGraphView = mGridLayout.findViewById(R.id.comp_graph_display_graph);

        mContainer.addView(mGridLayout);
        return mContainer;
    }

    @Override
    public void bindView(Context context, ComponentData componentData, LifecycleOwner lifecycleOwner) {
        final GraphDisplayComponentData data = (GraphDisplayComponentData) componentData;

        data.enablePolling(lifecycleOwner);
        update(data);
        data.valueLiveData().observe(lifecycleOwner, x -> update(data));


        //setup graph
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(data.getGraphMaxXValue());

        mGraphView.getGridLabelRenderer().setLabelVerticalWidth(80);

        // first mSeries is a line
        LineGraphSeries<DataPoint>[] s = data.getSeries();
        for(int i = s.length-1; i >= 0; i--){
            mGraphView.addSeries(s[i]);
        }

        // set date label formatter
        mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(
                mGraphView.getContext(), new SimpleDateFormat("HH:mm.ss", Locale.GERMANY)));
        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(4);
        mGraphView.getGridLabelRenderer().setHorizontalLabelsAngle(45);

        mGraphView.getViewport().setScalableY(false);
        mGraphView.getViewport().setScrollable(false);
    }

    private void update(GraphDisplayComponentData data){
        mNameTextView.setText(data.getName());
        mDisplayNameTextView.setText(data.getDisplayName());
        mValueActualTextView.setText(data.getActualFormatted());
        mValueNominalTextView.setText(data.getNominalFormatted());

        if(data.hasStatusText()){
            mStatusTextView.setText(data.getStatusText());
            mStatusTextView.setVisibility(View.VISIBLE);
        }else{
            mStatusTextView.setText(null);
            mStatusTextView.setVisibility(View.GONE);
        }

        int sev = data.getSeverity();
        int col = COLOR_BG_NORMAL;
        int text = R.string.value_display_text_normal;
        if (sev != ValueMonitorComponentData.SEVERITY_NORMAL) {
            if (sev == ValueMonitorComponentData.SEVERITY_WARNING) {
                col = COLOR_BG_WARNING;
                text = R.string.value_display_text_warning;
            } else if (sev == ValueMonitorComponentData.SEVERITY_ERROR) {
                col = COLOR_BG_ERROR;
                text = R.string.value_display_text_error;
            }
        }
        mStatusTextView.setForeground(new ColorDrawable(col));
        mStatusTextView.setText(text);

    }

    @Override
    public void dispose() {
        mDisplayNameTextView = null;
        mNameTextView = null;
        mValueNominalTextView = null;
        mValueActualTextView = null;
        mStatusTextView = null;
        mContainer = null;
        mGridLayout = null;
    }

    @Override
    public View getView() {
        return mContainer;
    }
}