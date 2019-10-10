package com.androidlabs.data.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.androidlabs.StaticMethods;

@Entity(tableName = StaticMethods.calculationsTitle, foreignKeys = {@ForeignKey(entity = Figure.class,
        parentColumns = "id", childColumns = "figureId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Data.class, parentColumns = "id", childColumns = "dataId", onDelete = ForeignKey.CASCADE)})
public class Calculations {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int figureId;

    public int dataId;

    public double area;

    public double perimeter;

    public static Calculations fromContentValues(ContentValues values) {
        final Calculations calculations = new Calculations();
        if (values.containsKey("id")) {
            calculations.id = values.getAsInteger("id");
        }
        if (values.containsKey("figureId")) {
            calculations.figureId = values.getAsInteger("figureId");
        }
        if (values.containsKey("dataId")) {
            calculations.dataId = values.getAsInteger("dataId");
        }
        if (values.containsKey("area")) {
            calculations.area = values.getAsDouble("area");
        }
        if (values.containsKey("perimeter")) {
            calculations.perimeter = values.getAsDouble("perimeter");
        }
        return calculations;
    }
}
