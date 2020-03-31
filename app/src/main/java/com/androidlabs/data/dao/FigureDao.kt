package com.androidlabs.data.dao

import android.database.Cursor
import androidx.room.*
import com.androidlabs.data.entity.Figure
import com.androidlabs.util.StaticMethods

@Dao
interface FigureDao {
    @get:Query("SELECT * FROM " + StaticMethods.figureTitle)
    val all: List<Figure?>?

    @get:Query("SELECT DISTINCT name FROM " + StaticMethods.figureTitle)
    val namesList: Array<String?>?

    @Query("SELECT name From " + StaticMethods.figureTitle + " WHERE id = :id")
    fun getNameById(id: Int): String?

    @Query("SELECT * FROM " + StaticMethods.figureTitle + " WHERE id = :id")
    fun getById(id: Int): Figure?

    @Query("SELECT id FROM " + StaticMethods.figureTitle + " WHERE name = :name")
    fun getIdByName(name: String?): Int

    @Query("SELECT * FROM " + StaticMethods.figureTitle)
    fun selectAll(): Cursor?

    @Query("SELECT * FROM " + StaticMethods.figureTitle + " WHERE id = :id")
    fun selectById(id: Int): Cursor?

    @Query("DELETE FROM " + StaticMethods.figureTitle + " WHERE id LIKE :id")
    fun deleteById(id: Int): Int

    @Query("DELETE FROM " + StaticMethods.figureTitle)
    fun removeAllFigures(): Int

    @Insert
    fun insert(figure: Figure?): Long

    @Update
    fun update(figure: Figure?): Int

    @Delete
    fun delete(figure: Figure?): Int
}