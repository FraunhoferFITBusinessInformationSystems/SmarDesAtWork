/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vh on 13.03.2018.
 */

public class MimeTypeUtils {

    private final static Map<String, List<String>> mimeMap = createMimeMap();

    private static Map<String, List<String>> createMimeMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("image/jpeg", Arrays.asList(".jpg", ".jpeg"));
        map.put("image/png", Arrays.asList(".png"));
        map.put("text/plain", Arrays.asList(".txt"));
        map.put("text/xml", Arrays.asList(".xml"));
        map.put("application/json", Arrays.asList(".json"));
        map.put("application/octet-stream", Arrays.asList(".obj", ".bin"));
        return map;
    }

    public static String getMimeType(String extension){
        for (Map.Entry<String, List<String>> entry : mimeMap.entrySet()) {
            for (String s : entry.getValue()) {
                if(s.equals(extension))
                    return entry.getKey();
            }
        }
        return getDefaultMimeType();
    }

    public static String getExtension(String mimeType){
        for (String key : mimeMap.keySet()) {
            if(mimeType.equals(key)){
                return mimeMap.get(key).get(0);
            }
        }
        return mimeMap.get(getDefaultMimeType()).get(0);
    }

    public static String getDefaultMimeType(){
        return "application/octet-stream";
    }
}
