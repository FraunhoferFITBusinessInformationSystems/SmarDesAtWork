/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.ui;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.vogler_engineering.smartdevicesapp.common.util.MapUtils;
import lombok.Data;

/**
 * Created by vh on 22.03.2018.
 */

@Data
public class UiComponent {
    private ComponentType type;
    private String id;
    private String name;

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

    public UiComponent() {
    }

    public UiComponent(ComponentType type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public UiComponent(UiComponent other){
        this.type = other.type;
        this.id = other.id;
        this.name = other.name;
        MapUtils.cloneMap(other.additionalProperties, this.additionalProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UiComponent)) return false;
        UiComponent other = (UiComponent) o;
        if (!Objects.equals(this.type, other.type)) return false;
        if (!Objects.equals(this.id, other.id)) return false;
        if (!Objects.equals(this.name, other.name)) return false;
        if (!MapUtils.equals(this.additionalProperties, other.additionalProperties)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
