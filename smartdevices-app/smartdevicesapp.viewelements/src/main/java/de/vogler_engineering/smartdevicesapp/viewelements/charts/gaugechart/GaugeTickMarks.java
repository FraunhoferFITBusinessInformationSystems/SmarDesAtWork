/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by vh on 15.02.2018.
 */

class GaugeTickMarks {

    //There are always the first and the last major tick, numMajorTicks=0 means there are 2 major tick marks.
    int numMajorTicks = 0;
    int numMajorTickDivisions = 0;
    String[] majorTickMarkLabel = null;
    int tickMarkColor = Color.GRAY;
    int labelColor = Color.BLACK;

    private final Paint tickPaint = new Paint();
    private final Paint majorTickPaint = new Paint();
    private final Paint labelPaint = new Paint();
    private final Paint circlePaint = new Paint();
    private TickMarkPos[] tickMarkPositions;

    private PointF center;
    private float circleRadius;
    private float angleMin, angleMax;

    private RectF circleBounds = new RectF();

    GaugeTickMarks() {
    }

    void initTickMarks(){
        tickPaint.reset();
        tickPaint.setColor(tickMarkColor);
        tickPaint.setStrokeWidth(5f);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        majorTickPaint.reset();
        majorTickPaint.setColor(tickMarkColor);
        majorTickPaint.setStrokeWidth(8f);
        majorTickPaint.setStrokeCap(Paint.Cap.ROUND);

        labelPaint.reset();
        labelPaint.setTextSize(32f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(labelColor);

        circlePaint.reset();
        circlePaint.setColor(tickMarkColor);
        circlePaint.setStrokeWidth(8f);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
    }

    void measureTickMarks(float innerRadius, PointF center, float angleMin, float angleMax){
        this.center = center;
        this.circleRadius = innerRadius;
        this.angleMin = angleMin+90;
        this.angleMax = angleMax+90;

        //Overall Tick count
        int tickCount;
        float tickAngle;

        float tickSize;
        float majorTickSize;

        tickSize = innerRadius*0.05f;
        majorTickSize = tickSize*2;

        tickCount = (numMajorTicks+1) * (numMajorTickDivisions+1);
        tickAngle = (angleMax-angleMin) / tickCount;

        tickMarkPositions = new TickMarkPos[tickCount+1];

        float majorRadius = innerRadius + majorTickSize;
        float minorRadius = innerRadius + tickSize;
        float labelRadius = innerRadius + majorTickSize + tickSize;

        double angleRadians;

        float x1, y1, x2, y2, xLab, yLab;
        boolean major;
        Paint.Align a;
        float aggregatedAngle = angleMin+90;
        for(int i = 0; i < tickMarkPositions.length; i++){
            major = (i % (numMajorTickDivisions+1) == 0);

            angleRadians = Math.toRadians(aggregatedAngle);
            x2 = center.x + (float)(innerRadius * Math.cos(angleRadians));
            y2 = center.y + (float)(innerRadius * Math.sin(angleRadians));

            x1 = center.x + (float)((major?majorRadius:minorRadius) * Math.cos(angleRadians));
            y1 = center.y + (float)((major?majorRadius:minorRadius) * Math.sin(angleRadians));

            if(major){
                xLab = center.x + (float)((labelRadius) * Math.cos(angleRadians));
                yLab = center.y + (float)((labelRadius) * Math.sin(angleRadians));
                if(aggregatedAngle == 90 || aggregatedAngle == 270){
                    a = Paint.Align.CENTER;
                }else if(aggregatedAngle > 90 && aggregatedAngle < 270){
                    a = Paint.Align.RIGHT;
                }else{
                    a = Paint.Align.LEFT;
                }
            }else{
                xLab = 0;
                yLab = 0;
                a = null;
            }
            tickMarkPositions[i] = new TickMarkPos(x1, y1, x2, y2, major, xLab, yLab, a);
            aggregatedAngle += tickAngle;
        }

        circleBounds.set(center.x - circleRadius, center.y - circleRadius, center.x + circleRadius, center.y + circleRadius);
    }

    void drawTickMarks(Canvas canvas, Gauge chart){
        canvas.save();

        int majorCount = 0;
        for(int i = 0; i < tickMarkPositions.length; i++){
            TickMarkPos pos = tickMarkPositions[i];

            canvas.drawLine(pos.x1, pos.y1, pos.x2, pos.y2, pos.major?majorTickPaint:tickPaint);
            if(pos.major){
                if(majorTickMarkLabel != null && majorCount < majorTickMarkLabel.length &&  majorTickMarkLabel[majorCount] != null) {
                    labelPaint.setTextAlign(pos.align);
                    canvas.drawText(majorTickMarkLabel[majorCount], pos.labelX, pos.labelY, labelPaint);
                }
                ++majorCount;
            }
        }


        canvas.drawArc(circleBounds, angleMin, angleMax-angleMin, false, circlePaint);

//        for(int i = 0; i <= tickCount; i++){
//            if(i % (numMajorTickDivisions+1) == 0) { // major tick mark
//                canvas.drawLine(startX, startY - majorTickSize, startX, startY, tickPaint);
//                //draw Major tick label
//            }else{
//                canvas.drawLine(startX, startY - tickSize, startX, startY, tickPaint);
//            }
//            canvas.rotate(tickAngle, centerX, centerY);
//
//
//            canvas.drawText("", );
//        }

        canvas.restore();
    }

    private class TickMarkPos{
        final float x1;
        final float y1;
        final float x2;
        final float y2;
        final boolean major;
        final float labelX, labelY;
        final Paint.Align align;

        private TickMarkPos(float x1, float y1, float x2, float y2, boolean major, float labelX, float labelY, Paint.Align align) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.major = major;
            this.labelX = labelX;
            this.labelY = labelY;
            this.align = align;
        }
    }
}
