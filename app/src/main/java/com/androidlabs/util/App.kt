package com.androidlabs.util

import android.app.Application
import androidx.room.Room
import com.androidlabs.data.AppDatabase

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this,
                AppDatabase::class.java, "database")
                .allowMainThreadQueries()
                .build()
    }
}