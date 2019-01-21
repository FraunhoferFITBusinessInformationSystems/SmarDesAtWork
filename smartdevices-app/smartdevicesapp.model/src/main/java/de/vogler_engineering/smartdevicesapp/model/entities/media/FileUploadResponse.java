/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.media;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import lombok.Data;

/**
 * Created by vh on 09.03.2018.
 */

@Data
public class FileUploadResponse {

    private boolean success;
    private UUID id;
    private long size;

    @JsonProperty("Content-Type")
    private String contentType;
}
