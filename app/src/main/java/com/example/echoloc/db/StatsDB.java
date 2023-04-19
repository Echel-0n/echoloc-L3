package com.example.echoloc.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * BDD
 */
@Database(entities = {Stats.class}, version = 1)
public abstract class StatsDB extends RoomDatabase {
    public abstract StatsDAO statDao();
}