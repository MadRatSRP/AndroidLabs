package com.androidlabs.data.entity

import android.content.ContentValues
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidlabs.util.StaticMethods

@Entity(tableName = StaticMethods.figureTitle)
class Figure {
    @kotlin.jvm.JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @kotlin.jvm.JvmField
    var name: String? = null

    companion object {
        fun fromContentValues(values: ContentValues?): Figure {
            val figure = Figure()
            if (values!!.containsKey("id")) {
                figure.id = values.getAsInteger("id").toLong()
            }
            if (values.containsKey("name")) {
                figure.name = values.getAsString("name")
            }
            return figure
        }
    }
}