/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by vh on 15.02.2018.
 */

abstract class Gauge extends View{

    protected final GaugeTickMarks tickMarks = new GaugeTickMarks();
    protected final GaugeNeedle needle = new GaugeNeedle();
    protected final GaugeSegments segments = new GaugeSegments();

    float minAngle;
    float maxAngle;

    protected int width, height;

    protected RectF internalBounds = new RectF();
    protected PointF center = new PointF();
    protected float radius;

    public Gauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.postInvalidate();

        initDefaultValues();
        init();
    }

    protected void initDefaultValues() {
        minAngle = 90;
        maxAngle = 270;

        needle.setAngle(0);
        needle.animationFps = 60;
        needle.animationDurationInMillis = 16;
        needle.needleColor = Color.BLACK;

        segments.segments = new GaugeSegment[] { new GaugeSegment(180, Color.YELLOW) };
        segments.arcWidth = 80;
        segments.fillCenter = false;
        segments.segmentSpacing = 2;
        segments.segmentOuterSpacing = 15;

        tickMarks.numMajorTicks = 0;
        tickMarks.numMajorTickDivisions = 0;
        tickMarks.majorTickMarkLabel = new String[] { "0", "100" };
        tickMarks.tickMarkColor = Color.GRAY;
        tickMarks.labelColor = Color.BLACK;
    }

    protected void init() {
        segments.initSegments();
        needle.initNeedle();
        tickMarks.initTickMarks();
    }

    protected void updateAll() {
        needle.initNeedle();
        segments.initSegments();
        tickMarks.initTickMarks();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        width = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));

        float internalWidth = width - getPaddingLeft() - getPaddingRight();
        float internalCenterX = internalWidth / 2;

        float angleLeft = 90 - minAngle;
        float angleRight = maxAngle - 270;
        float advancedAngle = Math.max(angleLeft, angleRight);
        if(advancedAngle < 0) advancedAngle = 0;
        float southHeight = (float) (internalCenterX * Math.tan(advancedAngle));
        float minSouthHeight = internalCenterX * 0.2f;
        if(southHeight < minSouthHeight) southHeight = minSouthHeight;

        int minh = (int)(internalCenterX + southHeight) + getPaddingBottom() + getPaddingTop();
        height = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);
        setMeasuredDimension(width, height);

        internalBounds.set(getPaddingLeft(), getPaddingTop(), getPaddingLeft()+width, getPaddingTop()+height);

        center.set(internalBounds.width()/2, internalBounds.height()+getPaddingTop()-southHeight);
        radius = Math.min(center.x, center.y);

        segments.measureSegments(radius*0.7f, center);
        tickMarks.measureTickMarks(radius*0.7f, center, minAngle, maxAngle);
        needle.measureNeedle(radius*0.65f, center);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        segments.drawSegments(canvas, this);
        needle.drawNeedle(canvas, this);
        tickMarks.drawTickMarks(canvas, this);
    }
}
