package com.androidlabs.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidlabs.data.dao.CalculationsDAO
import com.androidlabs.data.dao.DataDao
import com.androidlabs.data.dao.FigureDao
import com.androidlabs.data.entity.Calculations
import com.androidlabs.data.entity.Data
import com.androidlabs.data.entity.Figure

@Database(entities = [Figure::class, Calculations::class, Data::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun figureDao(): FigureDao
    abstract fun calculationsDAO(): CalculationsDAO
    abstract fun dataDao(): DataDao
}