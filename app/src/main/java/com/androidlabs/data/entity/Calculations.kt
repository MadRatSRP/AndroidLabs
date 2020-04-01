package com.androidlabs.data.entity

import android.content.ContentValues
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.androidlabs.util.StaticMethods

@Entity(tableName = StaticMethods.calculationsTitle,
        foreignKeys = [ForeignKey(
                entity = Figure::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("figureId"),
                onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Data::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("dataId"),
                    onDelete = ForeignKey.CASCADE)])
class Calculations {
    @kotlin.jvm.JvmField
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @kotlin.jvm.JvmField
    var figureId = 0
    @kotlin.jvm.JvmField
    var dataId = 0
    @kotlin.jvm.JvmField
    var area = 0.0
    @kotlin.jvm.JvmField
    var perimeter = 0.0

    companion object {
        fun fromContentValues(values: ContentValues?): Calculations {
            val calculations = Calculations()
            if (values!!.containsKey("id")) {
                calculations.id = values.getAsInteger("id")
            }
            if (values.containsKey("figureId")) {
                calculations.figureId = values.getAsInteger("figureId")
            }
            if (values.containsKey("dataId")) {
                calculations.dataId = values.getAsInteger("dataId")
            }
            if (values.containsKey("area")) {
                calculations.area = values.getAsDouble("area")
            }
            if (values.containsKey("perimeter")) {
                calculations.perimeter = values.getAsDouble("perimeter")
            }
            return calculations
        }
    }
}