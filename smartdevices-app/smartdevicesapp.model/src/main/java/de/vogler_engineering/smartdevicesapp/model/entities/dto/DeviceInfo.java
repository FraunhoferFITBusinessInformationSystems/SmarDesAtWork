/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.dto;

import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.entities.enums.DeviceFamily;
import de.vogler_engineering.smartdevicesapp.model.entities.enums.DeviceType;
import lombok.Data;

/**
 * Created by vh on 14.02.2018.
 */

@Data
public class DeviceInfo {
    private String fcmToken;
//    private String apiVersion;
    private DeviceType type;
    private DeviceFamily family;
    private String deviceName;

    private Map<String, String> properties;

    public Map<String, String> getProperties() {
        if(properties == null)
            properties = new HashMap<>();
        return properties;
    }
}
