package com.androidlabs.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.androidlabs.data.dao.CalculationsDAO;
import com.androidlabs.data.dao.DataDao;
import com.androidlabs.data.dao.FigureDao;
import com.androidlabs.data.entity.Calculations;
import com.androidlabs.data.entity.Data;
import com.androidlabs.data.entity.Figure;

@Database(entities = {Figure.class, Calculations.class, Data.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FigureDao figureDao();
    public abstract CalculationsDAO calculationsDAO();
    public abstract DataDao dataDao();
}
