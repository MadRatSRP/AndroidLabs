package com.androidlabs.data.dao

import android.database.Cursor
import androidx.room.*
import com.androidlabs.data.entity.Calculations
import com.androidlabs.util.StaticMethods

@Dao
interface CalculationsDAO {
    @get:Query("SELECT * FROM " + StaticMethods.calculationsTitle)
    val all: List<Calculations?>?

    @Query("SELECT * FROM " + StaticMethods.calculationsTitle + " WHERE id = :id")
    fun getById(id: Long): Calculations?

    //Удаление по Id
    @Query("DELETE FROM " + StaticMethods.calculationsTitle + " WHERE id LIKE :id")
    fun deleteById(id: Int): Int

    @Query("DELETE FROM " + StaticMethods.calculationsTitle)
    fun removeAllRows(): Int

    @Query("SELECT * FROM " + StaticMethods.calculationsTitle)
    fun selectAll(): Cursor?

    @Query("SELECT * FROM " + StaticMethods.calculationsTitle + " WHERE id = :id")
    fun selectById(id: Int): Cursor?

    @Insert
    fun insert(calculations: Calculations?): Long

    @Update
    fun update(calculations: Calculations?): Int

    @Delete
    fun delete(calculations: Calculations?): Int
}