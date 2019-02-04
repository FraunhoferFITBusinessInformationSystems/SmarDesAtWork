/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities.resources;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.Data;

@Entity
@Data
public class StoredResource {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "request_uri")
    private String requestUri;

    @ColumnInfo(name = "filename")
    private String filename;

    @ColumnInfo(name = "mime_type")
    private String mimeType;

    @ColumnInfo(name = "width")
    private Integer width;

    @ColumnInfo(name = "heigth")
    private Integer height;

    @ColumnInfo(name = "size")
    private int size;

    @ColumnInfo(name = "local_path")
    private int localPath;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "last_access")
    private Date lastAccess;

    @ColumnInfo(name = "last_update")
    private Date lastUpdate;

    @ColumnInfo(name = "lifetime")
    private Integer lifetime;

}
