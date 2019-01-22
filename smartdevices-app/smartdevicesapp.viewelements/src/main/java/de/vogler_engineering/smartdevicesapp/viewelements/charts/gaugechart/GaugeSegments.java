/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by vh on 15.02.2018.
 */

class GaugeSegments {
    GaugeSegment[] segments;
    float arcWidth;
    boolean fillCenter;
    float segmentOuterSpacing;
    float segmentSpacing;

    private RectF bounds;
    private Paint segmentWithCenterPaint, segmentNoCenterPaint;

    GaugeSegments(){
        this.segmentWithCenterPaint = new Paint();
        this.segmentNoCenterPaint = new Paint();

        this.bounds = new RectF();

        initSegments();
    }

    void initSegments() {
        segmentWithCenterPaint.reset();
        segmentWithCenterPaint.setAntiAlias(true);
        segmentWithCenterPaint.setDither(true);
        segmentWithCenterPaint.setStyle(Paint.Style.FILL);

        segmentNoCenterPaint.reset();
        segmentNoCenterPaint.setAntiAlias(true);
        segmentNoCenterPaint.setDither(true);
        segmentNoCenterPaint.setStrokeWidth(arcWidth);
        segmentNoCenterPaint.setStyle(Paint.Style.FILL);
        segmentNoCenterPaint.setStyle(Paint.Style.STROKE);
    }

    void measureSegments(float radius, PointF center){
        float arcRadius = radius- arcWidth/2 - segmentOuterSpacing;
        bounds.set(center.x - arcRadius, center.y - arcRadius, center.x + arcRadius, center.y + arcRadius);

//            float innerArcRadius = arcRadius - arcWidth;
//            arcInnerBounds.set(centerX - innerArcRadius, centerY - innerArcRadius, centerX + innerArcRadius, centerY + innerArcRadius);
    }

    void drawSegments(Canvas canvas, Gauge chart){
        //90° angle offset, drawArc starts right with 0°
        canvas.save();
        float fromAngle = chart.minAngle + 90;

        for(GaugeSegment seg : segments) {

            if(fillCenter) {
                segmentWithCenterPaint.setColor(seg.getColor());
                canvas.drawArc(bounds, fromAngle+segmentSpacing/2, seg.getAngle()-segmentSpacing, true, segmentWithCenterPaint);
            }else{
                segmentNoCenterPaint.setColor(seg.getColor());
                canvas.drawArc(bounds, fromAngle+segmentSpacing/2, seg.getAngle()-segmentSpacing, false, segmentNoCenterPaint);
            }
            fromAngle += seg.getAngle();
        }
        canvas.restore();
    }
}
