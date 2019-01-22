/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.util;

import java.util.Map;

import de.vogler_engineering.smartdevicesapp.common.util.StringUtil;
import de.vogler_engineering.smartdevicesapp.model.entities.job.Job;

public class JobUtils {
    private JobUtils() {
    }

    //TODO move this to ui-definition in sdgw
    public static String getTypeName(Job job) {
        Map<String, String> res = job.getResource();
        String name = job.getName();
        if(name == null) name = "";
        if (res != null) {
            switch (name) {
                default:
                    String r = getFromRes(res, "list_title");
                    if (r != null) return r;
            }
        }
        if(job.getName() != null) {
            return "Auftrag " + job.getName();
        }
        return "JobId: " + job.getId();
    }

    //TODO move this to ui-definition in sdgw
    public static String getSubtitle(Job job) {
        Map<String, String> res = job.getResource();
        String name = job.getName();
        if(name == null) name = "";
        if (res != null) {
            String r = "";
            switch (name) {
                default:
                    r = getFromRes(res, "list_text");
                    if (r != null) return r;
                    r = getFromRes(res, "text");
                    if (r != null) return r;
            }
        }
        return "";
    }

    public static String getFromRes(Map<String, String> res, String key) {
        String k = StringUtil.firstToLowerCase(key);
        if (res.containsKey(k))
            return res.get(k);
        k = StringUtil.firstToUpperCase(key);
        if (res.containsKey(k))
            return res.get(k);
        return null;
    }

}
