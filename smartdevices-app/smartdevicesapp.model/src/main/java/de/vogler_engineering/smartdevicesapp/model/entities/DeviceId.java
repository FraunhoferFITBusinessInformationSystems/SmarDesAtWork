/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Created by vh on 14.02.2018.
 */

@Data
public class DeviceId {

    private static String ID_KEY_SPACER = ".";

    private final String username;
    private final String deviceId;

    @JsonIgnore
    public String getIdKey(){
        return String.format("%s%s%s", username, ID_KEY_SPACER, deviceId);
    }

    public DeviceId(String username, String deviceId){
        this.username = username;
        this.deviceId = deviceId;
    }

    public DeviceId(String idKey){
        int dot = idKey.indexOf(ID_KEY_SPACER);
        if(dot <= 0 || dot == idKey.length()-1 || idKey.indexOf(ID_KEY_SPACER, dot+1) != -1)
            throw new IllegalArgumentException("Cannot parse DeviceIdKey");
        this.username = idKey.substring(0, dot);
        this.deviceId = idKey.substring(dot+1);
    }
}
