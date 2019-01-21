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
public class PaginationRequest implements RetrofitRequest {
    private int pageNumber;
    private int pageSize;

    @JsonIgnore
    @Override
    public Map<String, String> buildRequest() {
        Map<String, String> map = new HashMap<>();
        map.put("PageNumber", String.valueOf(pageNumber));
        map.put("PageSize", String.valueOf(pageSize));
        return map;
    }

    public static PaginationRequest buildPaginationRequest(SharedPreferences pref, String tabKey){
        final String menuKey = PreferenceUtils.MENU_TYPE_PAGE;
        PaginationRequest r = new PaginationRequest();
        r.setPageNumber(pref.getInt(getMenuPrefVarName(tabKey, menuKey, PreferenceUtils.VALUE_NAME_PNUMBER), 1));
        r.setPageSize(pref.getInt(getMenuPrefVarName(tabKey, menuKey, PreferenceUtils.VALUE_NAME_PSIZE), 100));
        return r;
    }
}
