/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import de.vogler_engineering.smartdevicesapp.model.entities.resources.StoredResource;

@Database(entities = {
        StoredResource.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private final static String DATABASE_NAME = "smartdevicesapp-db";

    public abstract StoredResourceDao storedResourceDao();

    public static AppDatabase createInstance(Context applicationContext){
        return Room.databaseBuilder(applicationContext,
                AppDatabase.class, DATABASE_NAME).build();
    }
}