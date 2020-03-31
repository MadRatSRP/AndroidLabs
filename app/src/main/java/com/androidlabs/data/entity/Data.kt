package com.androidlabs.data.entity

import android.content.ContentValues
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidlabs.util.StaticMethods
import java.io.Serializable

@Entity(tableName = StaticMethods.dataTitle)
class Data : Serializable {
    @kotlin.jvm.JvmField
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @kotlin.jvm.JvmField
    var width: Double? = null
    @kotlin.jvm.JvmField
    var height: Double? = null
    @kotlin.jvm.JvmField
    var side: Double? = null
    @kotlin.jvm.JvmField
    var radius: Double? = null

    companion object {
        fun fromContentValues(values: ContentValues?): Data {
            val data = Data()
            if (values!!.containsKey("id")) {
                data.id = values.getAsInteger("id")
            }
            if (values.containsKey("width")) {
                data.width = values.getAsDouble("width")
            }
            if (values.containsKey("height")) {
                data.height = values.getAsDouble("height")
            }
            if (values.containsKey("side")) {
                data.side = values.getAsDouble("side")
            }
            if (values.containsKey("radius")) {
                data.radius = values.getAsDouble("radius")
            }
            return data
        }
    }
}