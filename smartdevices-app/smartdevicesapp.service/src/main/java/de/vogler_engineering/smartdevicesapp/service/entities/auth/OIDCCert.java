/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.service.entities.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Created by vh on 20.02.2018.
 */

@Data
public class OIDCCert {
    @JsonProperty("kit")
    private String kid;

    @JsonProperty("kty")
    private String kty;

    @JsonProperty("alg")
    private String alg;

    @JsonProperty("use")
    private String use;

    @JsonProperty("n")
    private String n;

    @JsonProperty("e")
    private String e;
}
