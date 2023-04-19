package com.example.echoloc.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Statistic dans la base de données
 */
@Entity
public class Stats {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int sid;

    @ColumnInfo(name = "game_mode")
    public int gameMode;

    @ColumnInfo(name = "success")
    public boolean success;

    @ColumnInfo(name = "city_name")
    public String cityName;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "replay")
    public int replay;

    @ColumnInfo(name = "buttons_count")
    public int buttonsCount;

    @ColumnInfo(name = "distance")
    public String distance;

    public Stats(int gameMode, boolean success, String cityName, long duration, int replay, int buttonsCount, String distance){
        this.gameMode = gameMode;
        this.success = success;
        this.cityName = cityName;
        this.duration = duration;
        this.replay = replay;
        this.buttonsCount = buttonsCount;
        this.distance = distance;
    }
}