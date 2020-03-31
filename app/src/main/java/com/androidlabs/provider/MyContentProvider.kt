package com.androidlabs.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import com.androidlabs.data.AppDatabase
import com.androidlabs.data.dao.CalculationsDAO
import com.androidlabs.data.dao.DataDao
import com.androidlabs.data.dao.FigureDao
import com.androidlabs.data.entity.Calculations
import com.androidlabs.data.entity.Data
import com.androidlabs.data.entity.Figure
import com.androidlabs.util.App
import com.androidlabs.util.StaticMethods

class MyContentProvider : ContentProvider() {
    companion object {
        // Адрес к папке с MyContentProvider
        const val AUTHORITY = "com.androidlabs.provider"

        // URI для таблицы Calculations
        val URI_CALCULATIONS = Uri.parse(
                "content://" + AUTHORITY + "/" + StaticMethods.calculationsTitle
        )

        // URI для таблицы Data
        val URI_DATA = Uri.parse(
                "content://" + AUTHORITY + "/" + StaticMethods.dataTitle
        )

        // URI для таблицы Figure
        val URI_FIGURE = Uri.parse(
                "content://" + AUTHORITY + "/" + StaticMethods.figureTitle
        )

        // Код для группы айтемов из таблицы Calculations
        const val CODE_CALCULATIONS_DIR = 1

        // Код для айтема из таблицы Calculations
        const val CODE_CALCULATIONS_ITEM = 2

        // Код для группы айтемов из таблицы Data
        const val CODE_DATA_DIR = 3

        // Код для айтема из таблицы Data
        const val CODE_DATA_ITEM = 4

        // Код для группы айтемов из таблицы Figure
        const val CODE_FIGURE_DIR = 5

        // Код для айтема из таблицы Figure
        const val CODE_FIGURE_ITEM = 6

        // URI Matcher
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        init {
            // Calculations
            MATCHER.addURI(AUTHORITY, StaticMethods.calculationsTitle, CODE_CALCULATIONS_DIR)
            MATCHER.addURI(AUTHORITY, StaticMethods.calculationsTitle + "/*", CODE_CALCULATIONS_ITEM)
            // Data
            MATCHER.addURI(AUTHORITY, StaticMethods.dataTitle, CODE_DATA_DIR)
            MATCHER.addURI(AUTHORITY, StaticMethods.dataTitle + "/*", CODE_DATA_ITEM)
            // Figure
            MATCHER.addURI(AUTHORITY, StaticMethods.figureTitle, CODE_FIGURE_DIR)
            MATCHER.addURI(AUTHORITY, StaticMethods.figureTitle + "/*", CODE_FIGURE_ITEM)
        }
    }

    var db: AppDatabase? = null
    var figureDao: FigureDao? = null
    var dataDao: DataDao? = null
    var calculationsDAO: CalculationsDAO? = null
    var data: Data? = null
    var calculations: Calculations? = null
    var figure: Figure? = null
    var bundle: Bundle? = null
    var context: Context? = null
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?,
                       selection: String?, selectionArgs: Array<String>?,
                       sortOrder: String?, cancellationSignal: CancellationSignal?): Cursor? {
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?,
                       queryArgs: Bundle?, cancellationSignal: CancellationSignal?): Cursor? {
        return super.query(uri, projection, queryArgs, cancellationSignal)
    }

    override fun query(uri: Uri, projection: Array<String>?,
                       selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        context = getContext()
        if (context == null) {
            return null
        }
        calculationsDAO = App.Companion.getInstance().getDatabase().calculationsDAO()
        val code = MATCHER.match(uri)
        // Calculations
        return if (code == CODE_CALCULATIONS_DIR || code == CODE_CALCULATIONS_ITEM) {
            val cursor: Cursor?
            cursor = if (code == CODE_CALCULATIONS_DIR) {
                calculationsDAO!!.selectAll()
            } else {
                calculationsDAO!!.selectById(ContentUris.parseId(uri).toInt())
            }
            cursor!!.setNotificationUri(context!!.contentResolver, uri)
            cursor
        } else if (code == CODE_DATA_DIR || code == CODE_DATA_ITEM) {
            dataDao = App.Companion.getInstance().getDatabase().dataDao()
            val cursor: Cursor?
            cursor = if (code == CODE_DATA_DIR) {
                dataDao!!.selectAll()
            } else {
                dataDao!!.selectById(ContentUris.parseId(uri).toInt())
            }
            cursor!!.setNotificationUri(context!!.contentResolver, uri)
            cursor
        } else if (code == CODE_FIGURE_DIR || code == CODE_FIGURE_ITEM) {
            figureDao = App.Companion.getInstance().getDatabase().figureDao()
            val cursor: Cursor?
            cursor = if (code == CODE_FIGURE_DIR) {
                figureDao!!.selectAll()
            } else {
                figureDao!!.selectById(ContentUris.parseId(uri).toInt())
            }
            cursor!!.setNotificationUri(context!!.contentResolver, uri)
            cursor
        } else {
            throw IllegalArgumentException("Неизвестный URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (MATCHER.match(uri)) {
            CODE_CALCULATIONS_DIR -> "vnd.android.cursor.dir/" + AUTHORITY + "." + StaticMethods.calculationsTitle
            CODE_CALCULATIONS_ITEM -> "vnd.android.cursor.item/" + AUTHORITY + "." + StaticMethods.calculationsTitle
            CODE_DATA_DIR -> "vnd.android.cursor.dir/" + AUTHORITY + "." + StaticMethods.dataTitle
            CODE_DATA_ITEM -> "vnd.android.cursor.item/" + AUTHORITY + "." + StaticMethods.dataTitle
            CODE_FIGURE_DIR -> "vnd.android.cursor.dir/" + AUTHORITY + "." + StaticMethods.figureTitle
            CODE_FIGURE_ITEM -> "vnd.android.cursor.item/" + AUTHORITY + "." + StaticMethods.figureTitle
            else -> throw IllegalArgumentException("Неизвестный URI: $uri")
        }
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        db = App.Companion.getInstance().getDatabase()
        figureDao = db!!.figureDao()
        dataDao = db!!.dataDao()
        when (method) {
            "getFigureId" -> {
                bundle = Bundle()
                val id = figureDao!!.getIdByName(arg)
                bundle!!.putInt("figureId", id)
                return bundle
            }
            "getFigureName" -> {
                bundle = Bundle()
                val name = figureDao!!.getNameById(Integer.valueOf(arg!!))
                bundle!!.putString("name", name)
                return bundle
            }
            "getDataByDataId" -> {
                bundle = Bundle()
                val id = Integer.valueOf(arg!!)
                data = dataDao!!.getDataById(id)
                bundle!!.putSerializable("data", data)
                return bundle
            }
            "getFiguresNames" -> {
                bundle = Bundle()
                val spinnerItems = figureDao.getNamesList()
                bundle!!.putStringArray("spinnerItems", spinnerItems)
                return bundle
            }
            "getIdBySide" -> {
                bundle = Bundle()
                val side = java.lang.Double.valueOf(arg!!)
                val id = dataDao!!.getIdBySide(side)
                bundle!!.putInt("id", id)
                return bundle
            }
            "getIdByRadius" -> {
                bundle = Bundle()
                val radius = java.lang.Double.valueOf(arg!!)
                val id = dataDao!!.getIdByRadius(radius)
                bundle!!.putInt("id", id)
                return bundle
            }
            "getIdByWidthAndHeight" -> {
                bundle = Bundle()
                val width = extras!!.getInt("width").toDouble()
                val height = extras.getInt("height").toDouble()
                val id = dataDao!!.getIdByWidthAndHeight(width, height)
                bundle!!.putInt("id", id)
                return bundle
            }
        }
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        context = getContext()
        return if (context == null) {
            null
        } else when (MATCHER.match(uri)) {
            CODE_CALCULATIONS_DIR -> {
                val id = App.Companion.getInstance().getDatabase().calculationsDAO()
                        .insert(Calculations.Companion.fromContentValues(values)) as Int
                context!!.contentResolver.notifyChange(uri, null)
                ContentUris.withAppendedId(uri, id.toLong())
            }
            CODE_CALCULATIONS_ITEM -> insertCalculations(uri, values)
            CODE_DATA_DIR -> {
                val id_data = App.Companion.getInstance().getDatabase().dataDao()
                        .insert(Data.Companion.fromContentValues(values)) as Int
                context!!.contentResolver.notifyChange(uri, null)
                ContentUris.withAppendedId(uri, id_data.toLong())
            }
            CODE_DATA_ITEM -> insertData(uri, values)
            CODE_FIGURE_DIR -> {
                val id_figure = App.Companion.getInstance().getDatabase().figureDao()
                        .insert(Figure.Companion.fromContentValues(values)) as Int
                context!!.contentResolver.notifyChange(uri, null)
                ContentUris.withAppendedId(uri, id_figure.toLong())
            }
            CODE_FIGURE_ITEM -> insertFigure(uri, values)
            else -> throw IllegalArgumentException("Неизвестный URI: $uri")
        }
    }

    private fun insertData(uri: Uri, values: ContentValues?): Uri {
        db = App.Companion.getInstance().getDatabase()
        dataDao = db!!.dataDao()
        data = Data()
        data!!.width = values!!.getAsDouble("width")
        data!!.height = values.getAsDouble("height")
        data!!.side = values.getAsDouble("side")
        data!!.radius = values.getAsDouble("radius")
        val id = dataDao!!.insert(data)
        return ContentUris.withAppendedId(uri, id)
    }

    private fun insertCalculations(uri: Uri, values: ContentValues?): Uri {
        db = App.Companion.getInstance().getDatabase()
        calculationsDAO = db!!.calculationsDAO()
        figureDao = db!!.figureDao()
        calculations = Calculations()
        val spinnerText: String
        val figureId: Int
        if (values!!.getAsInteger("figureId") == null) {
            spinnerText = values.getAsString("spin")
            figureId = figureDao!!.getIdByName(spinnerText)
            calculations!!.figureId = figureId
        } else {
            calculations!!.figureId = values.getAsInteger("figureId")
        }

        //calculations.figureId =
        calculations!!.dataId = values.getAsInteger("dataId")
        calculations!!.area = values.getAsDouble("area")
        calculations!!.perimeter = values.getAsDouble("perimeter")
        val id = calculationsDAO!!.insert(calculations)
        return ContentUris.withAppendedId(uri, id)
    }

    private fun insertFigure(uri: Uri, values: ContentValues?): Uri {
        db = App.Companion.getInstance().getDatabase()
        figureDao = db!!.figureDao()
        figure = Figure()
        figure!!.name = values!!.getAsString("name")
        val id = figureDao!!.insert(figure)
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(uri: Uri, values: ContentValues?,
                        selection: String?, selectionArgs: Array<String>?): Int {
        context = getContext()
        return if (context == null) {
            0
        } else when (MATCHER.match(uri)) {
            CODE_CALCULATIONS_DIR ->                 //throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
                updateCalculation(uri, values)
            CODE_CALCULATIONS_ITEM -> {
                calculations = Calculations.Companion.fromContentValues(values)
                calculations!!.id = ContentUris.parseId(uri).toInt()
                val count: Int = App.Companion.getInstance().getDatabase().calculationsDAO()
                        .update(calculations)
                context!!.contentResolver.notifyChange(uri, null)
                count
            }
            CODE_DATA_DIR -> throw IllegalArgumentException("Invalid URI, cannot update without ID$uri")
            CODE_DATA_ITEM -> {
                data = Data.Companion.fromContentValues(values)
                data!!.id = ContentUris.parseId(uri).toInt()
                val count_data: Int = App.Companion.getInstance().getDatabase().dataDao()
                        .update(data)
                context!!.contentResolver.notifyChange(uri, null)
                count_data
            }
            CODE_FIGURE_DIR -> throw IllegalArgumentException("Invalid URI, cannot update without ID$uri")
            CODE_FIGURE_ITEM -> {
                figure = Figure.Companion.fromContentValues(values)
                figure!!.id = ContentUris.parseId(uri) as Int.toLong()
                val count_figure: Int = App.Companion.getInstance().getDatabase().figureDao()
                        .update(figure)
                context!!.contentResolver.notifyChange(uri, null)
                count_figure
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        context = getContext()
        return if (context == null) {
            0 /*(int) ContentUris.parseId(uri)*/
        } else when (MATCHER.match(uri)) {
            CODE_CALCULATIONS_DIR -> removeAllCalculations()
            CODE_CALCULATIONS_ITEM -> {
                val count: Int = App.Companion.getInstance().getDatabase().calculationsDAO()
                        .deleteById(Integer.valueOf(uri.lastPathSegment!!))
                context!!.contentResolver.notifyChange(uri, null)
                count
            }
            CODE_DATA_DIR -> throw IllegalArgumentException("Invalid URI, cannot update without ID$uri")
            CODE_DATA_ITEM -> {
                val count_data: Int = App.Companion.getInstance().getDatabase().dataDao()
                        .deleteById(ContentUris.parseId(uri).toInt())
                context!!.contentResolver.notifyChange(uri, null)
                count_data
            }
            CODE_FIGURE_DIR -> removeAllFigures()
            CODE_FIGURE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot update without ID$uri")
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    private fun removeAllFigures(): Int {
        db = App.Companion.getInstance().getDatabase()
        figureDao = db!!.figureDao()
        return figureDao!!.removeAllFigures()
    }

    private fun removeAllCalculations(): Int {
        db = App.Companion.getInstance().getDatabase()
        calculationsDAO = db!!.calculationsDAO()
        return calculationsDAO!!.removeAllRows()
    }

    private fun removeCalculation(uri: Uri, selection: String): Int {
        db = App.Companion.getInstance().getDatabase()
        calculationsDAO = db!!.calculationsDAO()

        //long id = Long.valueOf(uri.getLastPathSegment());


        //calculationsDAO.deleteById((int) id);
        return calculationsDAO!!.deleteById(Integer.valueOf(selection))
    }

    private fun updateCalculation(uri: Uri, values: ContentValues?): Int {
        db = App.Companion.getInstance().getDatabase()
        calculationsDAO = db!!.calculationsDAO()
        calculations = Calculations()
        calculations!!.id = values!!.getAsInteger("id")
        calculations!!.dataId = values.getAsInteger("dataId")
        calculations!!.figureId = values.getAsInteger("figureId")
        calculations!!.area = values.getAsDouble("area")
        calculations!!.perimeter = values.getAsDouble("perimeter")
        calculationsDAO!!.update(calculations)
        return calculations!!.id
    }
}