/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.requests;

import android.content.SharedPreferences;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

import de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils;
import lombok.Data;

import static de.vogler_engineering.smartdevicesapp.model.util.PreferenceUtils.getMenuPrefVarName;

@Data
public class SortRequest implements RetrofitRequest{
    private String sortBy;
    private boolean sortOrderAscending;

    @JsonIgnore
    @Override
    public Map<String, String> buildRequest() {
        Map<String, String> map = new HashMap<>();
        map.put("SortBy", sortBy==null?"":sortBy);
        map.put("SortOrderAscending", String.valueOf(sortOrderAscending));
        return map;
    }

    public static SortRequest buildSortRequest(SharedPreferences pref, String tabKey){
        final String menuKey = PreferenceUtils.MENU_TYPE_SORT;
        SortRequest s = new SortRequest();

        String asc = pref.getString(getMenuPrefVarName(tabKey, menuKey, PreferenceUtils.VALUE_NAME_ASC, null), PreferenceUtils.VALUE_ASC);
        s.setSortOrderAscending(!PreferenceUtils.VALUE_DESC.equals(asc));
        s.setSortBy(pref.getString(getMenuPrefVarName(tabKey, menuKey, PreferenceUtils.VALUE_NAME_KEY, null), null));
        return s;
    }
}
