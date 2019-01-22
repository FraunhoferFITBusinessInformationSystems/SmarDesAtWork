/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import lombok.Data;

/**
 * Created by vh on 14.02.2018.
 */

@Data
public class DeviceConfig {
    private AppInfo info;

    private String deviceId;
    private String deviceName;
    private String user;
    private String dataUrl;

    private boolean reconfigure;

    @JsonProperty("tabs")
    private List<TabConfig> tabConfig;
}
