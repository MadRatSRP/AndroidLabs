package com.androidlabs.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidlabs.StaticMethods;
import com.androidlabs.data.AppDatabase;
import com.androidlabs.data.dao.CalculationsDAO;
import com.androidlabs.data.dao.DataDao;
import com.androidlabs.data.dao.FigureDao;
import com.androidlabs.data.entity.Calculations;
import com.androidlabs.data.entity.Data;
import com.androidlabs.data.entity.Figure;
import com.androidlabs.util.App;

import java.io.Serializable;

public class MyContentProvider extends ContentProvider {
    // Адрес к папке с MyContentProvider
    public static final String AUTHORITY = "com.androidlabs.provider";

    // URI для таблицы Calculations
    public static final Uri URI_CALCULATIONS = Uri.parse(
            "content://" + AUTHORITY + "/" + StaticMethods.calculationsTitle
    );
    // URI для таблицы Data
    public static final Uri URI_DATA = Uri.parse(
            "content://" + AUTHORITY + "/" + StaticMethods.dataTitle
    );
    // URI для таблицы Figure
    public static final Uri URI_FIGURE = Uri.parse(
            "content://" + AUTHORITY + "/" + StaticMethods.figureTitle
    );

    // Код для группы айтемов из таблицы Calculations
    public static final int CODE_CALCULATIONS_DIR = 1;
    // Код для айтема из таблицы Calculations
    public static final int CODE_CALCULATIONS_ITEM = 2;

    // Код для группы айтемов из таблицы Data
    public static final int CODE_DATA_DIR = 3;
    // Код для айтема из таблицы Data
    public static final int CODE_DATA_ITEM = 4;

    // Код для группы айтемов из таблицы Figure
    public static final int CODE_FIGURE_DIR = 5;
    // Код для айтема из таблицы Figure
    public static final int CODE_FIGURE_ITEM = 6;

    // URI Matcher
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Calculations
        MATCHER.addURI(AUTHORITY, StaticMethods.calculationsTitle, CODE_CALCULATIONS_DIR);
        MATCHER.addURI(AUTHORITY, StaticMethods.calculationsTitle + "/*", CODE_CALCULATIONS_ITEM);
        // Data
        MATCHER.addURI(AUTHORITY, StaticMethods.dataTitle, CODE_DATA_DIR);
        MATCHER.addURI(AUTHORITY, StaticMethods.dataTitle + "/*", CODE_DATA_ITEM);
        // Figure
        MATCHER.addURI(AUTHORITY, StaticMethods.figureTitle, CODE_FIGURE_DIR);
        MATCHER.addURI(AUTHORITY, StaticMethods.figureTitle + "/*", CODE_FIGURE_ITEM);
    }

    AppDatabase db;
    FigureDao figureDao;
    DataDao dataDao;
    CalculationsDAO calculationsDAO;

    Data data;
    Calculations calculations;
    Figure figure;

    Bundle bundle;
    Context context;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder, @Nullable CancellationSignal cancellationSignal) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable Bundle queryArgs, @Nullable CancellationSignal cancellationSignal) {
        return super.query(uri, projection, queryArgs, cancellationSignal);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        context = getContext();
        if (context == null) {
            return null;
        }

        calculationsDAO = App.getInstance().getDatabase().calculationsDAO();

        final int code = MATCHER.match(uri);
        // Calculations
        if (code == CODE_CALCULATIONS_DIR || code == CODE_CALCULATIONS_ITEM) {
            final Cursor cursor;
            if (code == CODE_CALCULATIONS_DIR) {
                cursor = calculationsDAO.selectAll();
            } else {
                cursor = calculationsDAO.selectById((int) ContentUris.parseId(uri));
            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        }
        // Data
        else if (code == CODE_DATA_DIR || code == CODE_DATA_ITEM) {
            dataDao = App.getInstance().getDatabase().dataDao();
            final Cursor cursor;
            if (code == CODE_DATA_DIR) {
                cursor = dataDao.selectAll();
            } else {
                cursor = dataDao.selectById((int) ContentUris.parseId(uri));
            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        }
        // Figure
        else if (code == CODE_FIGURE_DIR || code == CODE_FIGURE_ITEM) {
            figureDao = App.getInstance().getDatabase().figureDao();
            final Cursor cursor;
            if (code == CODE_FIGURE_DIR) {
                cursor = figureDao.selectAll();
            } else {
                cursor = figureDao.selectById((int) ContentUris.parseId(uri));
            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        }
        //Обработка ошибки
        else {
            throw new IllegalArgumentException("Неизвестный URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            // Calculations
            case CODE_CALCULATIONS_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + StaticMethods.calculationsTitle;
            case CODE_CALCULATIONS_ITEM:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + StaticMethods.calculationsTitle;

            // Data
            case CODE_DATA_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + StaticMethods.dataTitle;
            case CODE_DATA_ITEM:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + StaticMethods.dataTitle;

            // Figure
            case CODE_FIGURE_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + StaticMethods.figureTitle;
            case CODE_FIGURE_ITEM:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + StaticMethods.figureTitle;
            default:
                throw new IllegalArgumentException("Неизвестный URI: " + uri);
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        db = App.getInstance().getDatabase();
        figureDao = db.figureDao();
        dataDao = db.dataDao();

        switch (method) {
            case "getFigureId": {
                bundle = new Bundle();

                int id = figureDao.getIdByName(arg);

                bundle.putInt("figureId", id);

                return bundle;
            }
            case "getFigureName":

                bundle = new Bundle();

                String name = figureDao.getNameById(Integer.valueOf(arg));

                bundle.putString("name", name);

                return bundle;
            case "getDataByDataId": {
                bundle = new Bundle();

                int id = Integer.valueOf(arg);

                data = dataDao.getDataById(id);


                bundle.putSerializable("data", data);

                return bundle;
            }
            case "getFiguresNames": {
                bundle = new Bundle();
                String[] spinnerItems = figureDao.getNamesList();

                bundle.putStringArray("spinnerItems", spinnerItems);
                return bundle;
            }
            case "getIdBySide": {
                bundle = new Bundle();
                double side = Double.valueOf(arg);
                int id = dataDao.getIdBySide(side);
                bundle.putInt("id", id);
                return bundle;
            }

            case "getIdByRadius": {
                bundle = new Bundle();
                double radius = Double.valueOf(arg);
                int id = dataDao.getIdByRadius(radius);
                bundle.putInt("id", id);
                return bundle;
            }

            case "getIdByWidthAndHeight": {
                bundle = new Bundle();
                double width = (double) extras.getInt("width");
                double height = (double) extras.getInt("height");
                int id = dataDao.getIdByWidthAndHeight(width, height);
                bundle.putInt("id", id);
                return bundle;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        context = getContext();
        if (context == null) {
            return null;
        }

        switch (MATCHER.match(uri)) {
            // Calculations
            case CODE_CALCULATIONS_DIR:
                final int id = (int) App.getInstance().getDatabase().calculationsDAO()
                        .insert(Calculations.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            case CODE_CALCULATIONS_ITEM:
                return insertCalculations(uri, values);
            // Data
            case CODE_DATA_DIR:
                final int id_data = (int) App.getInstance().getDatabase().dataDao()
                        .insert(Data.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id_data);
            case CODE_DATA_ITEM:
                return insertData(uri, values);
            // Figure
            case CODE_FIGURE_DIR:
                final int id_figure = (int) App.getInstance().getDatabase().figureDao()
                        .insert(Figure.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id_figure);
            case CODE_FIGURE_ITEM:
                return insertFigure(uri, values);
            default:
                throw new IllegalArgumentException("Неизвестный URI: " + uri);
        }
    }

    private Uri insertData(Uri uri, ContentValues values) {
        db = App.getInstance().getDatabase();
        dataDao = db.dataDao();

        data = new Data();
        data.width = values.getAsDouble("width");
        data.height = values.getAsDouble("height");
        data.side = values.getAsDouble("side");
        data.radius = values.getAsDouble("radius");

        long id = dataDao.insert(data);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertCalculations(Uri uri, ContentValues values) {
        db = App.getInstance().getDatabase();
        calculationsDAO = db.calculationsDAO();
        figureDao = db.figureDao();

        calculations = new Calculations();

        String spinnerText;

        int figureId;

        if (values.getAsInteger("figureId") == null) {
            spinnerText = values.getAsString("spin");
            figureId = figureDao.getIdByName(spinnerText);
            calculations.figureId = figureId;
        } else {
            calculations.figureId = values.getAsInteger("figureId");
        }

        //calculations.figureId =
        calculations.dataId = values.getAsInteger("dataId");
        calculations.area = values.getAsDouble("area");
        calculations.perimeter = values.getAsDouble("perimeter");

        long id = calculationsDAO.insert(calculations);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertFigure(Uri uri, ContentValues values) {
        db = App.getInstance().getDatabase();
        figureDao = db.figureDao();

        figure = new Figure();
        figure.name = values.getAsString("name");

        long id = figureDao.insert(figure);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        context = getContext();
        if (context == null) {
            return 0;
        }

        switch (MATCHER.match(uri)) {
            // Calculations
            case CODE_CALCULATIONS_DIR:
                //throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
                return updateCalculation(uri, values);

            case CODE_CALCULATIONS_ITEM:
                calculations = Calculations.fromContentValues(values);
                calculations.id = (int) ContentUris.parseId(uri);
                final int count = App.getInstance().getDatabase().calculationsDAO()
                        .update(calculations);
                context.getContentResolver().notifyChange(uri, null);
                return count;
            // Data
            case CODE_DATA_DIR:
                throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
            case CODE_DATA_ITEM:
                data = Data.fromContentValues(values);
                data.id = (int) ContentUris.parseId(uri);
                final int count_data = App.getInstance().getDatabase().dataDao()
                        .update(data);
                context.getContentResolver().notifyChange(uri, null);
                return count_data;
            // Figure
            case CODE_FIGURE_DIR:
                throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
            case CODE_FIGURE_ITEM:
                figure = Figure.fromContentValues(values);
                figure.id = (int) ContentUris.parseId(uri);
                final int count_figure = App.getInstance().getDatabase().figureDao()
                        .update(figure);
                context.getContentResolver().notifyChange(uri, null);
                return count_figure;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        context = getContext();
        if (context == null) {
            return 0;/*(int) ContentUris.parseId(uri)*/
        }

        switch (MATCHER.match(uri)) {
            // Calculations
            case CODE_CALCULATIONS_DIR:
                return removeAllCalculations();
            case CODE_CALCULATIONS_ITEM:
                final int count = App.getInstance().getDatabase().calculationsDAO()
                        .deleteById(Integer.valueOf(uri.getLastPathSegment()));
                context.getContentResolver().notifyChange(uri, null);
                return count;

            // Data
            case CODE_DATA_DIR:
                throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
            case CODE_DATA_ITEM:
                final int count_data = App.getInstance().getDatabase().dataDao()
                        .deleteById((int) ContentUris.parseId(uri));
                context.getContentResolver().notifyChange(uri, null);
                return count_data;

            // Figure
            case CODE_FIGURE_DIR:
                return removeAllFigures();
            case CODE_FIGURE_ITEM:
                throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private int removeAllFigures() {
        db = App.getInstance().getDatabase();
        figureDao = db.figureDao();

        return figureDao.removeAllFigures();
    }
    private int removeAllCalculations() {
        db = App.getInstance().getDatabase();
        calculationsDAO = db.calculationsDAO();

        return calculationsDAO.removeAllRows();
    }

    private int removeCalculation(Uri uri, String selection) {
        db = App.getInstance().getDatabase();
        calculationsDAO = db.calculationsDAO();

        //long id = Long.valueOf(uri.getLastPathSegment());


        //calculationsDAO.deleteById((int) id);
        return calculationsDAO.deleteById(Integer.valueOf(selection));
    }

    private int updateCalculation(Uri uri, ContentValues values) {
        db = App.getInstance().getDatabase();
        calculationsDAO = db.calculationsDAO();

        calculations = new Calculations();
        calculations.id = values.getAsInteger("id");
        calculations.dataId = values.getAsInteger("dataId");
        calculations.figureId = values.getAsInteger("figureId");
        calculations.area = values.getAsDouble("area");
        calculations.perimeter = values.getAsDouble("perimeter");

        calculationsDAO.update(calculations);

        return calculations.id;
    }
}
