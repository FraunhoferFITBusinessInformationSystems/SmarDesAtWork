/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Created by vh on 15.02.2018.
 */

class GaugeNeedle{
    //Desired Angle
    private float angle;

    private final Paint needlePaint;
    private final Path needleLinePath;
    private final Matrix needleRotation;

    //The current displayed Angle
    private float animationAngle;

    //Time after this the animation should be finished
    int animationDurationInMillis;

    //Update / Invalidation time of the View element
    int animationFps;

    private long animationStartTime;
    private boolean animationActive;

    int needleColor = Color.BLACK;
    private PointF center;

    GaugeNeedle(){

        //Setup Needle path
        needleLinePath = new Path();
        needlePaint = new Paint();
        needleRotation = new Matrix();

        initNeedle();
    }

    void initNeedle() {

        needlePaint.reset();
        needlePaint.setColor(needleColor);
        needlePaint.setAntiAlias(true);
        needlePaint.setShader(new RadialGradient(0, 0, 10.0f,
                Color.DKGRAY, needleColor, Shader.TileMode.CLAMP));

        needleRotation.reset();
    }

    void setAngle(float angle) {
        float angleDiff = this.angle - angle;
        if(angleDiff != 0) {
            this.animationAngle = this.angle;
            this.angle = angle;
            this.animationActive = true;
            this.animationStartTime = System.currentTimeMillis();
        }
    }

    void measureNeedle(float needleSize, PointF center){
        //Size from the Needle Path
        this.center = center;

        float lr = needleSize / 15;
        float back = needleSize / 5;
        float s = needleSize / 100;
        needleLinePath.reset();
        needleLinePath.moveTo(-lr , 0);

        needleLinePath.lineTo(-s, needleSize-s);
        needleLinePath.lineTo(0, needleSize);
        needleLinePath.lineTo(s, needleSize-s);

        needleLinePath.lineTo(lr, 0);
        needleLinePath.lineTo(s, -back+s);
        needleLinePath.lineTo(0, -back);
        needleLinePath.lineTo(-s, -back+s);

        needleLinePath.lineTo(-lr , 0);
        needleLinePath.close();
    }

    void drawNeedle(Canvas canvas, Gauge chart) {
        canvas.save();

        needleRotation.reset();
        needleRotation.postRotate(angle, 0, 0);
        needleRotation.postTranslate(center.x, center.y);

        canvas.concat(needleRotation);
        canvas.drawPath(needleLinePath, needlePaint);
        canvas.drawCircle(0, 0, 16.0f, needlePaint);

        if(animationActive){
            long elapsedTime = System.currentTimeMillis() - animationStartTime;
            if(elapsedTime > animationDurationInMillis) {
                animationActive = false;
                animationAngle = angle;
            }else{
                long remainingTime = animationDurationInMillis - elapsedTime;
                float deltaAngle = (angle-animationAngle) / remainingTime;
                animationAngle += deltaAngle;

                chart.postInvalidateDelayed(10000 / animationFps);
            }
        }
        canvas.restore();
    }
}
