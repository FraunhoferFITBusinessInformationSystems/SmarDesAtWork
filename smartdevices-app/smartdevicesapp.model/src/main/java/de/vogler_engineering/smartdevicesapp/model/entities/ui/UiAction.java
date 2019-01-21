/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.ui;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UiAction extends UiComponent {
    private String jobKey;

    public static UiAction fromComponent(UiComponent element) {
        if(!element.getAdditionalProperties().containsKey("JobKey") &&
                !element.getAdditionalProperties().containsKey("jobKey"))
            return null;

        UiAction action = new UiAction();
        action.setId(element.getId());
        action.setName(element.getName());
        action.setType(element.getType());

        for (Map.Entry<String, Object> entry :
                element.getAdditionalProperties().entrySet()) {
            if(entry.getKey().equalsIgnoreCase("JobKey")){
                action.setJobKey((String) entry.getValue());
            }else {
                action.setAdditionalProperty(entry.getKey(), entry.getValue());
            }
        }
        return action;
    }
}
