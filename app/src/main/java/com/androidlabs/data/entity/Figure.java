package com.androidlabs.data.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.androidlabs.util.StaticMethods;

@Entity(tableName = StaticMethods.figureTitle)
public class Figure {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public static Figure fromContentValues(ContentValues values) {
        final Figure figure = new Figure();
        if (values.containsKey("id")) {
            figure.id = values.getAsInteger("id");
        }
        if (values.containsKey("name")) {
            figure.name = values.getAsString("name");
        }
        return figure;
    }
}
