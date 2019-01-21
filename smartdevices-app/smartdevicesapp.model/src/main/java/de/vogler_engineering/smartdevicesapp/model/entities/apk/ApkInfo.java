/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.apk;

import java.util.List;

import lombok.Data;

@Data
public class ApkInfo {

    private LatestApkInfo latest;
    private List<String> all;

    @Data
    public class LatestApkInfo {
        private String phone;
        private String watch;
    }
}

