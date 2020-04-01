package com.androidlabs.util

import android.app.Application
import androidx.room.Room
import com.androidlabs.data.AppDatabase

class App : Application() {
    companion object {
        var instance: App? = null
    }

    var database: AppDatabase? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this,
                AppDatabase::class.java, "database")
                .allowMainThreadQueries()
                .build()
    }
}