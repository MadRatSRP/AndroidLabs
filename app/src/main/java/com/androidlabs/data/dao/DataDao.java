package com.androidlabs.data.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.androidlabs.StaticMethods;
import com.androidlabs.data.entity.Data;

@Dao
public interface DataDao {
    @Query("SELECT id From " + StaticMethods.dataTitle + " Where radius = :radius")
    int getIdByRadius(Double radius);

    @Query("SELECT id From " + StaticMethods.dataTitle + " Where side = :side")
    int getIdBySide(Double side);

    @Query("SELECT id From " + StaticMethods.dataTitle + " Where width = :width AND height = :height")
    int getIdByWidthAndHeight(Double width, Double height);

    @Query("SELECT * FROM " + StaticMethods.dataTitle + " WHERE id = :id")
    Data getDataById(int id);

    @Query("SELECT * FROM " + StaticMethods.dataTitle )
    Cursor selectAll();

    @Query("SELECT * FROM " + StaticMethods.dataTitle + " WHERE id = :id")
    Cursor selectById(int id);

    @Query("DELETE FROM " + StaticMethods.dataTitle + " WHERE id = :id")
    int deleteById(int id);

    @Insert
    long insert(Data data);

    @Update
    int update(Data data);

    @Delete
    int delete(Data data);
}
