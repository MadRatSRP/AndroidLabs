package com.androidlabs.data.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.androidlabs.util.StaticMethods;
import com.androidlabs.data.entity.Calculations;

import java.util.List;

@Dao
public interface CalculationsDAO {
    @Query("SELECT * FROM " + StaticMethods.calculationsTitle)
    List<Calculations> getAll();

    @Query("SELECT * FROM " + StaticMethods.calculationsTitle + " WHERE id = :id")
    Calculations getById(long id);

    //Удаление по Id
    @Query("DELETE FROM " + StaticMethods.calculationsTitle + " WHERE id LIKE :id")
    int deleteById(int id);

    @Query("DELETE FROM " + StaticMethods.calculationsTitle)
    int removeAllRows();

    @Query("SELECT * FROM " + StaticMethods.calculationsTitle )
    Cursor selectAll();

    @Query("SELECT * FROM " + StaticMethods.calculationsTitle + " WHERE id = :id")
    Cursor selectById(int id);

    @Insert
    long insert(Calculations calculations);

    @Update
    int update(Calculations calculations);

    @Delete
    int delete(Calculations calculations);
}
