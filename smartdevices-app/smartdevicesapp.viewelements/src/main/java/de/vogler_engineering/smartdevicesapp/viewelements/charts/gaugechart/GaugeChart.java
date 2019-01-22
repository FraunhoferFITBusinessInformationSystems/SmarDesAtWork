/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by vh on 16.02.2018.
 */

public class GaugeChart extends Gauge {

    public GaugeChart(Context context) {
        this(context, null);
    }

    public GaugeChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GaugeChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSegments(GaugeSegment[] segments) {
        this.segments.segments = segments;
    }

    /**
     * Sets the number of Major tickmarks between the left- and rightmost tickmarks. 0 Means, there are only the first and the last marks.
     * @param numMajorTicks The number of Major-tick-marks, without the first or the last, starting with 0.
     */
    public void setMajorTickCount(int numMajorTicks){
        if(numMajorTicks < 0) throw new IllegalArgumentException();
        this.tickMarks.numMajorTicks = numMajorTicks;
    }

    /**
     * Setting the number of divisions, in which the segments between major ticks are divided.
     * @param numMajorTickDivisions The number of divisions, starting with 0.
     */
    public void setMajorTickDivisions(int numMajorTickDivisions){
        if(numMajorTickDivisions < 0) throw new IllegalArgumentException();
        this.tickMarks.numMajorTickDivisions = numMajorTickDivisions;
    }

    /**
     * Sets the list of major-tick-mark labels.
     * @param majorTickMarkLabels Array of Label-Strings, with a length of MajorTickCount + 2 (for the first and the last tick).
     */
    public void setMajorTickMarkLabels(String[] majorTickMarkLabels){
        this.tickMarks.majorTickMarkLabel = majorTickMarkLabels;
    }

    public void setMaxAngle(float maxAngle) {
        this.maxAngle = maxAngle;
        updateAll();
        this.requestLayout();
    }

    public void setMinAngle(float minAngle) {
        this.minAngle = minAngle;
        updateAll();
        this.requestLayout();
    }

    public void setAngle(float angle){
        this.needle.setAngle(angle);
        this.invalidate();
    }

    public void animationFps(int fps){
        this.needle.animationFps = fps;
        this.needle.initNeedle();
        this.requestLayout();
    }

    public void setAnimationDuration(int millis){
        this.needle.animationDurationInMillis = millis;
    }

    public void setSegmentWidth(int segmentWidth){
        this.segments.arcWidth = segmentWidth;
        this.segments.initSegments();
        this.requestLayout();
    }

    public void setFillCenter(boolean fillCenter){
        this.segments.fillCenter = fillCenter;
        this.segments.initSegments();
        this.requestLayout();
    }

    public void setNeedleColor(int color){
        this.needle.needleColor = color;
        this.needle.initNeedle();
        this.requestLayout();
    }

    public void setSegmentSpacing(float segmentSpacing){
        this.segments.segmentSpacing = segmentSpacing;
        this.invalidate();
    }

    public void setSegmentOuterSpacing(float segmentOuterSpacing){
        this.segments.segmentOuterSpacing = segmentOuterSpacing;
        this.requestLayout();
    }

    public void setTickMarkColor(int color){
        tickMarks.tickMarkColor = color;
        tickMarks.initTickMarks();
        requestLayout();
    }

    public void setLabelColor(int color){
        tickMarks.labelColor = color;
        tickMarks.initTickMarks();
        requestLayout();
    }

    public float getMinAngle() {
        return minAngle;
    }

    public float getMaxAngle() {
        return maxAngle;
    }
}
