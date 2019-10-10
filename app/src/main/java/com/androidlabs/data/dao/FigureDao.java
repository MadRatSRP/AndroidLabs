package com.androidlabs.data.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.androidlabs.util.StaticMethods;
import com.androidlabs.data.entity.Figure;

import java.util.List;

@Dao
public interface FigureDao {
    @Query("SELECT * FROM " + StaticMethods.figureTitle)
    List<Figure> getAll();

    @Query("SELECT DISTINCT name FROM " + StaticMethods.figureTitle)
    String[] getNamesList();

    @Query("SELECT name From " + StaticMethods.figureTitle + " WHERE id = :id")
    String getNameById(int id);

    @Query("SELECT * FROM " + StaticMethods.figureTitle +  " WHERE id = :id")
    Figure getById(int id);

    @Query("SELECT id FROM " + StaticMethods.figureTitle + " WHERE name = :name")
    int getIdByName(String name);

    @Query("SELECT * FROM " + StaticMethods.figureTitle)
    Cursor selectAll();

    @Query("SELECT * FROM " + StaticMethods.figureTitle + " WHERE id = :id")
    Cursor selectById(int id);

    @Query("DELETE FROM " + StaticMethods.figureTitle + " WHERE id LIKE :id")
    int deleteById(int id);

    @Query("DELETE FROM " + StaticMethods.figureTitle)
    int removeAllFigures();

    @Insert
    long insert(Figure figure);

    @Update
    int update(Figure figure);

    @Delete
    int delete(Figure figure);
}
