/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public interface RetrofitRequest {
    @JsonIgnore
    Map<String, String> buildRequest();
}
