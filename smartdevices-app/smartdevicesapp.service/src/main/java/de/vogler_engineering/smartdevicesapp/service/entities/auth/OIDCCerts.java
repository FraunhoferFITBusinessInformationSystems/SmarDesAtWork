/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.service.entities.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

/**
 * Created by vh on 20.02.2018.
 */

@Data
public class OIDCCerts {
    @JsonProperty("keys")
    List<OIDCCert> keys;
}
