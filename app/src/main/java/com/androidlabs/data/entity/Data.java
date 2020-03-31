package com.androidlabs.data.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.androidlabs.util.StaticMethods;

import java.io.Serializable;

@Entity(tableName = StaticMethods.dataTitle)
public class Data implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public Double width;
    public Double height;
    public Double side;
    public Double radius;

    public static Data fromContentValues(ContentValues values) {
        final Data data = new Data();
        if (values.containsKey("id")) {
            data.id = values.getAsInteger("id");
        }
        if (values.containsKey("width")) {
            data.width = values.getAsDouble("width");
        }
        if (values.containsKey("height")) {
            data.height = values.getAsDouble("height");
        }
        if (values.containsKey("side")) {
            data.side = values.getAsDouble("side");
        }
        if (values.containsKey("radius")) {
            data.radius = values.getAsDouble("radius");
        }
        return data;
    }
}
