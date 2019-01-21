/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.requests;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import lombok.Data;
import lombok.Getter;

import static de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils.POSTFIX_ENABLED;
import static de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils.POSTFIX_INVERTED;
import static de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils.getMenuPrefVarName;

public class FilterRequest implements RetrofitRequest {

    @Getter
    public ArrayList<SingleFilterRequest> filterRequests = new ArrayList<>();

    public void addFilter(String by, String string, boolean excluding){
        SingleFilterRequest sfr = new SingleFilterRequest();
        sfr.setFilterBy(by);
        sfr.setFilterString(string);
        sfr.setFilterExcluding(excluding);
        filterRequests.add(sfr);
    }

    @SuppressLint("DefaultLocale")
    @JsonIgnore
    @Override
    public Map<String, String> buildRequest() {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < filterRequests.size(); i++){
            SingleFilterRequest sfr =filterRequests.get(i);
            map.put(String.format("FilterBy[%d]", i), sfr.getFilterBy());
            map.put(String.format("FilterString[%d]", i), sfr.getFilterString());
            map.put(String.format("FilterExcluding[%d]", i), String.valueOf(sfr.isFilterExcluding()));
        }
        return map;
    }

    @Data
    public class SingleFilterRequest {
        private String filterBy;
        private String filterString;
        private boolean filterExcluding;
    }

    public static FilterRequest buildFilterRequest(SharedPreferences pref, String tabKey, String[] valueNames){
        final String menuKey = PreferenceUtils.MENU_TYPE_FILTER;
        FilterRequest s = new FilterRequest();

        for (String valueName : valueNames) {
            boolean b = pref.getBoolean(getMenuPrefVarName(tabKey, menuKey, valueName, POSTFIX_ENABLED), false);
            if (b) {
                boolean inverted = pref.getBoolean(getMenuPrefVarName(tabKey, menuKey, valueName, POSTFIX_INVERTED), false);
                String str = pref.getString(getMenuPrefVarName(tabKey, menuKey, valueName), null);
                s.addFilter(valueName, str, inverted);
            }
        }
        return s;
    }
}
