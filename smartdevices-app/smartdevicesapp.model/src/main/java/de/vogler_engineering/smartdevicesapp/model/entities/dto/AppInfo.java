/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.dto;

import java.util.Objects;

import lombok.Data;

@Data
public class AppInfo {
    private String id;
    private String title;
    private String subtitle;
    private String titleResource;
    private String theme;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppInfo)) return false;
        AppInfo appInfo = (AppInfo) o;
        return Objects.equals(id, appInfo.id) &&
                Objects.equals(title, appInfo.title) &&
                Objects.equals(subtitle, appInfo.subtitle) &&
                Objects.equals(titleResource, appInfo.titleResource) &&
                Objects.equals(theme, appInfo.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, subtitle, titleResource, theme);
    }
}
