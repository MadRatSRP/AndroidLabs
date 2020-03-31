package com.androidlabs.data.dao

import android.database.Cursor
import androidx.room.*
import com.androidlabs.data.entity.Data
import com.androidlabs.util.StaticMethods

@Dao
interface DataDao {
    @Query("SELECT id From " + StaticMethods.dataTitle + " Where radius = :radius")
    fun getIdByRadius(radius: Double?): Int

    @Query("SELECT id From " + StaticMethods.dataTitle + " Where side = :side")
    fun getIdBySide(side: Double?): Int

    @Query("SELECT id From " + StaticMethods.dataTitle + " Where width = :width AND height = :height")
    fun getIdByWidthAndHeight(width: Double?, height: Double?): Int

    @Query("SELECT * FROM " + StaticMethods.dataTitle + " WHERE id = :id")
    fun getDataById(id: Int): Data?

    @Query("SELECT * FROM " + StaticMethods.dataTitle)
    fun selectAll(): Cursor?

    @Query("SELECT * FROM " + StaticMethods.dataTitle + " WHERE id = :id")
    fun selectById(id: Int): Cursor?

    @Query("DELETE FROM " + StaticMethods.dataTitle + " WHERE id = :id")
    fun deleteById(id: Int): Int

    @Insert
    fun insert(data: Data?): Long

    @Update
    fun update(data: Data?): Int

    @Delete
    fun delete(data: Data?): Int
}