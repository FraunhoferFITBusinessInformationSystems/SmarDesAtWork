/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.charts.gaugechart;

import android.graphics.Color;

/**
 * Created by vh on 15.02.2018.
 */

public class GaugeSegment {

    private final float angle;
    private final int color;
    private final String name;

    public GaugeSegment(float angle, String color){
        this(angle, Color.parseColor(color));
    }

    public GaugeSegment(float angle, int argb){
        this(angle, argb, null);
    }

    public GaugeSegment(float angle, int argb, String name){
        this.angle = angle;
        this.color = argb;
        this.name = name;
    }

    public GaugeSegment(GaugeSegment other){
        this(other.angle, other.color, other.name);
    }

    public float getAngle() {
        return angle;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
}
