package com.example.echoloc.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * DAO des Statistic de la base de données
 */
@Dao
public interface StatsDAO {
    @Query("SELECT * FROM stats")
    List<Stats> getAll();

    @Query("SELECT * FROM stats WHERE game_mode=:gameMode")
    List<Stats> getByGameMode(int gameMode);

    @Query("SELECT * FROM stats WHERE game_mode=:gameMode ORDER BY buttons_count DESC, duration ASC")
    List<Stats> getByGameModeSorted(int gameMode);

    @Insert
    void insert(Stats stats);
    @Insert
    void insertAll(Stats... stats);

    @Delete
    void delete(Stats stats);

/*
    @Query("SELECT * FROM stat WHERE id=:id")
    Stat getOneStat(int id);*/
}