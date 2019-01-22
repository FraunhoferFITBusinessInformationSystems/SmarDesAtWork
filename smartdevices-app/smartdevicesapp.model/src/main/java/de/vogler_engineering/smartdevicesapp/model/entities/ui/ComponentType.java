/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.ui;

import com.fasterxml.jackson.annotation.JsonCreator;

import timber.log.Timber;

/**
 * Created by vh on 22.03.2018.
 */

public enum ComponentType {

    Button,
    DoubleButton,
    Spinner,
    Switch,

    BarcodeInput,

    TextInput,
    TextView,

    DateInput,
    DateView,

    PictureInput,
    PictureView,

    Spacer,

    ValueDisplayPlain,
    ValueDisplayGauge,
    ValueDisplayAdvanced,
    ValueMonitor,

    GenericAction,

    OptionDialog,

    JobListEntry,

    Unknown, NumberInput, ExternalUrl,
    ToggleableEntry, ValueMonitorSingle,

    GraphDisplay;

    private static final String TAG = "ComponentType";

    public static final ComponentType values[] = values();

    public static ComponentType valueOfOrDefault(String name) {
        return valueOfOrDefault(name, TextInput);
    }

    public static ComponentType valueOfOrDefault(String name, ComponentType pDefault) {
        try {
            return ComponentType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return pDefault;
        }
    }

    public int getId(){
        return this.ordinal();
    }

    public static ComponentType getTypeFromId(int id){
        return values[id];
    }

    @JsonCreator
    public static ComponentType safeValueOf(String string) {
        try {
            return ComponentType.valueOf(string);
        } catch (IllegalArgumentException e) {
            Timber.tag(TAG).e("Unknown GUI Component type %s! Fix this!", string);
            return Unknown;
        }
    }
}
