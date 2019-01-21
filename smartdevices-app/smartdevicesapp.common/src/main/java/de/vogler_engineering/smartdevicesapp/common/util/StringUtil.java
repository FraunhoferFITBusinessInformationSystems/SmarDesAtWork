/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by vh on 20.02.2018.
 */

public class StringUtil {

    private StringUtil(){

    }

    public static boolean isNotNullOrEmpty(String s){
        return !(s == null || s.isEmpty());
    }

    public static boolean isValidUrl(String s){
        try {
            new URI(s).parseServerAuthority();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String firstToLowerCase(String str) {
        if(str == null) return null;
        if(str.length() < 1) return str.toLowerCase();
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static String firstToUpperCase(String str) {
        if(str == null) return null;
        if(str.length() < 1) return str.toUpperCase();
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
