/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.graphics.Color;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.common.util.DateUtils;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValue;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueData;
import de.vogler_engineering.smartdevicesapp.model.entities.value.DynamicValueProperties;
import de.vogler_engineering.smartdevicesapp.model.entities.value.GenericValueData;
import de.vogler_engineering.smartdevicesapp.model.repository.DynamicValueRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import timber.log.Timber;

public class GraphDisplayComponentData extends ComponentData<GenericValueData> implements Injectable {

    private static final String TAG = "GraphDisplayComponentData";

    public final static int SEVERITY_NORMAL = 0;
    public final static int SEVERITY_WARNING = 1;
    public final static int SEVERITY_ERROR = 2;
    public final static int SEVERITY_INFORMATION = 3;

    private final static int VAL_ACTUAL = 0;
    private final static int VAL_NOMINAL = 1;
    private final static int VAL_HH = 2;
    private final static int VAL_H = 3;
    private final static int VAL_L = 4;
    private final static int VAL_LL = 5;

    @Inject
    DynamicValueRepository dynamicValueRepository;

    private final double[] values = new double[6];
    private final boolean[] active = new boolean[6];
    private int severity = 0;
    private String name = component.getName();
    private String displayName = "";
    private String unit = "";
    private String statusText = null;
    private DecimalFormat numberFormat = new DecimalFormat("#0.00");
    private DateFormat jsonDateFormat;

    private double graphLastXValue = 5d;
    private long graphLastXMillis = 0;
    private LineGraphSeries<DataPoint>[] mSeries;

    private DynamicValueData dvd;
    private DynamicValue dv;
    private DynamicValueProperties dvp;

    private int graphMaxYValue = 60 * 2 * 1000; //2 Minutes in ms as x delta;
    private int graphMaxDataPoints = 60 * 2 * 5; //Data points for 5 Minutes

    private final Observer<List<DynamicValueData>> observer;

    public GraphDisplayComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
        //noinspection unchecked
        mSeries = new LineGraphSeries[6];
        for(int i = 0; i < mSeries.length; i++){
            mSeries[i] = new LineGraphSeries<>();
            mSeries[i].setDrawDataPoints(false);
            mSeries[i].setDrawBackground(false);
        }
        mSeries[VAL_NOMINAL].setColor(Color.GRAY);
        mSeries[VAL_HH].setColor(Color.RED);
        mSeries[VAL_H].setColor(Color.YELLOW);
        mSeries[VAL_L].setColor(Color.YELLOW);
        mSeries[VAL_LL].setColor(Color.RED);

        value.postValue(null);
        jsonDateFormat = DateUtils.createJsonDateFormat();

        observer = dynamicValueDataList -> {
            if (dynamicValueDataList != null) {
                for (DynamicValueData dynamicValueData : dynamicValueDataList) {
                    DynamicValue dynamicValue = dynamicValueData.getValue();
                    if(dynamicValue != null && dynamicValue.getName().equals(this.component.getName())) {
                        onDynamicDataUpdated(dynamicValueData);
                    }
                }
            }
        };
    }

    private void onDynamicDataUpdated(DynamicValueData dynamicValueData){
        DynamicValue dynamicValue = dynamicValueData.getValue();
        if (dynamicValue.getVal() != null) {
            DynamicValueProperties val = dynamicValue.getVal();
            dvd = dynamicValueData;
            dv = dynamicValue;
            dvp = val;

            valueUpdated();
            value.postValue(dvd);
        }
    }

    @Override
    public void setResourceValue(String s) {
    }

    @Override
    public String getResourceValue() {
        return null;
    }


    public void enablePolling(LifecycleOwner lifecycleOwner) {
        if(dynamicValueRepository != null) {
            dynamicValueRepository.getDataObservable().observe(lifecycleOwner, observer);
            dynamicValueRepository.enablePolling();
            dynamicValueRepository.startPolling();
        }
    }

    private void valueUpdated() {
        Object obj;

        //Actual Value
        values[VAL_ACTUAL] = dvp.getValue() != null ? asDouble(dvp.getValue()) : 0;
        active[VAL_ACTUAL] = true;

        //Nominal Value
        readValueFromProperties(VAL_NOMINAL, "SetPoint");

        //HH Value
        readValueFromProperties(VAL_HH, "HH");

        //H Value
        readValueFromProperties(VAL_H, "H");

        //L Value
        readValueFromProperties(VAL_L, "L");

        //LL Value
        readValueFromProperties(VAL_LL, "LL");

        //Time
        Date date = null;
        obj = dvp.getFromAdditionalProperties("Time");
        if(obj != null){
            try {
                date = jsonDateFormat.parse(String.valueOf(obj));
            }catch (ParseException ignored) { }
        }
        if(date == null) date = new Date();

        //Add Time Series entry
        createDataPoint(date);

        //Unit
        obj = dvp.getFromAdditionalProperties("Unit");
        unit = obj != null ? String.valueOf(obj) : "";

        //DisplayName
        obj = dvp.getFromAdditionalProperties("Name");
        displayName = obj != null ? String.valueOf(obj) : "";

        //Name
        obj = dvp.getFromAdditionalProperties("ValueName");
        name = obj != null ? String.valueOf(obj) : component.getName();

        //Severity
        obj = dvp.getFromAdditionalProperties("Severity");
        if(obj != null) {
            String sev = String.valueOf(obj);
            if(sev.equalsIgnoreCase("Normal"))
                severity = SEVERITY_NORMAL;
            else if(sev.equalsIgnoreCase("Warning"))
                severity = SEVERITY_WARNING;
            else if(sev.equalsIgnoreCase("Error"))
                severity = SEVERITY_ERROR;
            else if(sev.equalsIgnoreCase("Information"))
                severity = SEVERITY_INFORMATION;
        }else severity = SEVERITY_NORMAL;

        //TODO Additional Info
    }

    private void createDataPoint(Date date) {

        if(graphLastXMillis == 0){
            graphLastXMillis = System.currentTimeMillis();
            graphLastXValue = graphLastXMillis/1000.0;
        }else{
            long millisNow = System.currentTimeMillis();
            long elapsedMillis = millisNow - graphLastXMillis;
            graphLastXMillis = millisNow;
            graphLastXValue += 1.0/(elapsedMillis/1000.0);
            Timber.tag(TAG).d("New data Point: (X: %f|Y: %f) Now: %d, Elapsed: %d", graphLastXValue, values[VAL_ACTUAL], millisNow, elapsedMillis);
        }
        long millis = System.currentTimeMillis();
        for(int i = 0; i < mSeries.length; i++) {
            if(active[i]) {
                mSeries[i].appendData(new DataPoint(millis, values[i]), true, graphMaxDataPoints);
            }
        }
    }

    private void readValueFromProperties(int idx, String key){
        Object obj = dvp.getFromAdditionalProperties(key);
        active[idx] = obj != null;
        values[idx] = obj != null ? asDouble(obj) : 0d;
    }

    private static double asDouble(Object obj){
        if(obj != null) {
            try {
                Double d = Double.class.cast(obj);
                if (d != null) {
                    return d;
                }
            } catch (ClassCastException ignored) {
            }
            String str = String.valueOf(obj);
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0d;
    }

    //Getters and Setters

    public String getDisplayName(){
        return displayName;
    }

    public String getName(){
        return name;
    }

    public String getStatusText(){
        return statusText;
    }

    public boolean hasStatusText(){
        return statusText != null;
    }

    public LineGraphSeries<DataPoint>[] getSeries() {
        return mSeries;
    }

    public int getSeverity() {
        return severity;
    }

    public String getNominalFormatted(){
        return numberFormat.format(getNominal()) + " " + unit;
    }

    public String getActualFormatted(){
        return numberFormat.format(getActual()) + " " + unit;
    }

    public double getNominal() {
        return values[VAL_NOMINAL];
    }

    public double getActual() {
        return values[VAL_ACTUAL];
    }

    public double getHH() {
        return values[VAL_HH];
    }

    public double getH() {
        return values[VAL_H];
    }

    public double getL() {
        return values[VAL_L];
    }

    public double getLL() {
        return values[VAL_LL];
    }

    public boolean isNominalActive(){
        return active[VAL_NOMINAL];
    }

    public boolean isHHActive(){
        return active[VAL_HH];
    }

    public boolean isHActive(){
        return active[VAL_H];
    }

    public boolean isLActive(){
        return active[VAL_L];
    }

    public boolean isLLActive(){
        return active[VAL_LL];
    }

    public int getGraphMaxDataPoints() {
        return graphMaxDataPoints;
    }

    public int getGraphMaxXValue() {
        return graphMaxYValue;
    }
}
