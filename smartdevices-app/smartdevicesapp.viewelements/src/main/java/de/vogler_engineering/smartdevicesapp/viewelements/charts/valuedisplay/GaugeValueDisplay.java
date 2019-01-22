/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.valuedisplay;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;

import de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart.GaugeChart;
import de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart.GaugeSegment;

/**
 * Created by vh on 16.02.2018.
 */

public class GaugeValueDisplay extends GaugeChart{

    public GaugeValueDisplay(Context context) {
        this(context, null);
    }

    public GaugeValueDisplay(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeValueDisplay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GaugeValueDisplay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setMinAngle(60);
        setMaxAngle(300);
    }

    private int lowColor = Color.rgb(104, 167, 10);
    private int midColor = Color.rgb(244, 199, 15);
    private int highColor = Color.rgb(223, 34, 19);
    private int normalColor = Color.rgb(195, 195, 195);

    private float minValue;
    private float maxValue;

    private boolean highValueSet = false;
    private boolean lowValueSet = false;
    private boolean midValueSet = false;

    private float highValue;
    private float midValue;
    private float lowValue;

    private String unit;
    private float value;

    private void updateAppearance(){
        float valueMultiplier = getValueMultiplier();

        //Generate Segments
        ArrayList<GaugeSegment> segments = new ArrayList<>();

        if(lowValueSet || midValueSet || highValueSet){
//            if(lowValueSet)


        }else{
            segments.add(new GaugeSegment(getMaxAngle()-getMinAngle(), normalColor));
        }
        this.setSegments(segments.toArray(new GaugeSegment[0]));



        updateValue();
    }

    private void updateValue(){
        float angle = (value-minValue) * getValueMultiplier() + getMinAngle();
        setAngle(angle);
    }

    private float getValueMultiplier(){
        float valueRange = maxValue - minValue;
        float angleRange = getMaxAngle() - getMinAngle();
        return angleRange / valueRange;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
        updateAppearance();
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        updateAppearance();
    }

    public float getHighValue() {
        return highValue;
    }

    public void setHighValue(float highValue) {
        this.highValue = highValue;
        this.highValueSet = true;
        updateAppearance();
    }

    public float getMidValue() {
        return midValue;
    }

    public void setMidValue(float midValue) {
        this.midValue = midValue;
        this.midValueSet = true;
        updateAppearance();
    }

    public float getLowValue() {
        return lowValue;
    }

    public void setLowValue(float lowValue) {
        this.lowValue = lowValue;
        this.lowValueSet = true;
        updateAppearance();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
        updateAppearance();
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        updateValue();
    }
}
