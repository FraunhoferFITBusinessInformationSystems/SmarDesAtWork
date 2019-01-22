/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.tabs;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Data;

@Data
public class TabConfig implements Comparable<TabConfig> {
    private String title;
    private String key;
    private boolean mainTab = false;
    private boolean showBadgeNumber = false;
    private String icon;
    private List<TabSortEntry> sortEntries = new ArrayList<>();
    private List<TabFilterEntry> filterEntries = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TabConfig)) return false;
        if (!super.equals(o)) return false;
        TabConfig tabConfig = (TabConfig) o;
        return mainTab == tabConfig.mainTab &&
                showBadgeNumber == tabConfig.showBadgeNumber &&
                Objects.equals(title, tabConfig.title) &&
                Objects.equals(key, tabConfig.key) &&
                Objects.equals(icon, tabConfig.icon) &&
                Objects.equals(sortEntries, tabConfig.sortEntries) &&
                Objects.equals(filterEntries, tabConfig.filterEntries);
    }

    public boolean deepEquals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TabConfig)) return false;
        if (!super.equals(o)) return false;
        TabConfig tabConfig = (TabConfig) o;
        return mainTab == tabConfig.mainTab &&
                showBadgeNumber == tabConfig.showBadgeNumber &&
                Objects.equals(title, tabConfig.title) &&
                Objects.equals(key, tabConfig.key) &&
                Objects.equals(icon, tabConfig.icon) &&
                Objects.deepEquals(sortEntries, tabConfig.sortEntries) &&
                Objects.deepEquals(filterEntries, tabConfig.filterEntries);
    }



    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, key, mainTab, showBadgeNumber, icon, sortEntries, filterEntries);
    }

    @Override
    public int compareTo(@NonNull TabConfig o) {
        if(Objects.equals(key, o.key)) return 0;
        if(key == null) return 1;
        return key.compareTo(o.key);
    }
}
