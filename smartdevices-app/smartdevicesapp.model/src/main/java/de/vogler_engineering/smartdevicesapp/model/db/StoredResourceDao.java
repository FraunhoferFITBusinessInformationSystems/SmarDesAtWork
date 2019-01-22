/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.vogler_engineering.smartdevicesapp.model.entities.resources.StoredResource;

@Dao
public interface StoredResourceDao {

    @Query("SELECT * FROM stored_res")
    List<StoredResource> getAll();

    @Query("SELECT * FROM stored_res WHERE uid IN (:userIds)")
    List<StoredResource> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM stored_res WHERE request_uri LIKE :uri LIMIT 1")
    StoredResource findByUri(String uri);

    @Insert
    long insert(StoredResource user);

    @Insert
    long[] insert(StoredResource... users);

    @Delete
    void delete(StoredResource user);

}
