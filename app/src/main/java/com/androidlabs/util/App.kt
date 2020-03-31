package com.androidlabs.util

import android.app.Application
import androidx.room.Room
import com.androidlabs.data.AppDatabase

class App : Application() {
    var database: AppDatabase? = null
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this,
                AppDatabase::class.java, "database")
                .allowMainThreadQueries()
                .build()
    }

    companion object {
        var instance: App? = null
    }
}