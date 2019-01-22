/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.value;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class DynamicValueProperties{

    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("ValueName")
    private String valueName;

    @JsonProperty("Type")
    private String type;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Object getFromAdditionalProperties(String key){
        if(additionalProperties.containsKey(key))
            return additionalProperties.get(key);
        return null;
    }
}
