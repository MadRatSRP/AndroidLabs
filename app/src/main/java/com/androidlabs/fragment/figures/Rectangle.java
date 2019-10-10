package com.androidlabs.fragment.figures;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.androidlabs.R;
import com.androidlabs.provider.MyContentProvider;
import com.androidlabs.util.App;
import com.androidlabs.data.AppDatabase;
import com.androidlabs.data.dao.CalculationsDAO;
import com.androidlabs.data.dao.DataDao;
import com.androidlabs.data.dao.FigureDao;
import com.androidlabs.data.entity.Calculations;
import com.androidlabs.data.entity.Data;
import com.google.android.material.snackbar.Snackbar;

import org.decimal4j.util.DoubleRounder;

public class Rectangle extends Fragment {
    private EditText width;
    private EditText height;
    private TextView areaResult;
    private TextView perimeterResult;

    private ImageButton clearFields;
    private Button calculateAndSaveIntoDB;

    private CalculationsDAO calculationsDAO;
    private DataDao dataDao;
    private int figureId;

    private Calculations calculations;

    private Integer precision;

    private SharedPreferences settings;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        width.setBackgroundResource(R.drawable.backtext);
        height.setBackgroundResource(R.drawable.backtext);

        clearFields.setBackgroundResource(R.drawable.ic_clear);
        clearFields.setOnClickListener(v -> {
            showLog(R.string.clearFieldsPressed);
            width.setText("");
            height.setText("");
            areaResult.setText("");
            perimeterResult.setText("");
            showLog(R.string.clearFieldsComplete);
        });

        calculateAndSaveIntoDB.setOnClickListener(view -> {
            showLog(R.string.calculateAndSaveIntoDBPressed);

            calculateArea(Double.valueOf(width.getText().toString()),
                    Double.valueOf(height.getText().toString()));
            calculatePerimeter(Double.valueOf(width.getText().toString()),
                    Double.valueOf(height.getText().toString()));
            saveIntoDatabase(Double.valueOf(width.getText().toString()),
                    Double.valueOf(height.getText().toString()));
        });
    }

    private void calculateArea(Double width, Double height) {
        double area = width * height;
        double preciseArea = DoubleRounder.round(area, precision);

        areaResult.setText(String.valueOf(preciseArea));
        showLog(R.string.calculateAreaComplete);
    }
    private void calculatePerimeter(Double width, Double height) {
        double perimeter = 2 * (width + height);
        double precisePerimeter = DoubleRounder.round(perimeter, precision);

        perimeterResult.setText(String.valueOf(precisePerimeter));
        showLog(R.string.calculatePerimeterComplete);
    }
    private void saveIntoDatabase(Double width, Double height) {
        double precisedWidth = width;
        double precisedHeight = height;
        double precisedSide = (double) 0;
        double precisedRadius = (double) 0;

        ContentValues data_values = new ContentValues();
        data_values.put("width", precisedWidth);
        data_values.put("height", precisedHeight);
        data_values.put("side", precisedSide);
        data_values.put("radius", precisedRadius);

        Uri dataUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_DATA, data_values
        );
        int id_data = Integer.valueOf(dataUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Data: " + id_data);

        ContentValues calculations_values = new ContentValues();
        calculations_values.put("figureId", this.figureId);
        calculations_values.put("dataId", id_data);
        calculations_values.put("area", Double.valueOf(areaResult.getText().toString()));
        calculations_values.put("perimeter", Double.valueOf(perimeterResult.getText().toString()));

        Uri calculationsUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_CALCULATIONS, calculations_values
        );
        int id_calculations = Integer.valueOf(calculationsUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Calculations: " + id_calculations);

        showLog(R.string.calculateAndSaveIntoDBComplete);
        showSnack(getView(), R.string.calculateAndSaveIntoDBComplete);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String rectangle = getContext().getString(R.string.rectangleTitle);

        getActivity().setTitle(rectangle);
        View view = inflater.inflate(R.layout.fragment_rectangle, container, false);

        AppDatabase db = App.getInstance().getDatabase();
        FigureDao figureDao = db.figureDao();
        calculationsDAO = db.calculationsDAO();
        dataDao = db.dataDao();
        figureId = figureDao.getIdByName(rectangle);
        calculations = new Calculations();

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        precision = Integer.valueOf(settings.getString("pre", "1"));

        String log = getContext().getString(R.string.precisionReturned, precision);
        Log.d(getClass().getSimpleName(), log);

        width = view.findViewById(R.id.rectangleWidth);
        height = view.findViewById(R.id.rectangleHeight);
        areaResult = view.findViewById(R.id.rectangleAreaResult);
        perimeterResult = view.findViewById(R.id.rectanglePerimeterResult);

        clearFields = view.findViewById(R.id.rectangleClearFields);
        calculateAndSaveIntoDB = view.findViewById(R.id.rectangleSaveIntoDatabase);
        return view;
    }

    private void showSnack(View view, Integer messageId) {
        //Подбираем текст из ресурсов по его id
        String message = getContext().getString(messageId);
        //Отображаем снэк
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
    private void showLog(Integer messageId) {
        //Подбираем текст из ресурсов по его id
        String message = getContext().getString(messageId);
        //Отображение сообщения в логах
        Log.d(getClass().getSimpleName(), message);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Сохраняем данные полей в настройки
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putString("width", width.getText().toString());
        prefEditor.putString("height", height.getText().toString());
        prefEditor.putString("area", areaResult.getText().toString());
        prefEditor.putString("perimeter", perimeterResult.getText().toString());
        prefEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Получаем данные полей из настроек
        String width = settings.getString("width", "0");
        String height = settings.getString("height", "0");
        String area = settings.getString("area", "0");
        String perimeter = settings.getString("perimeter", "0");
        //Присваиваем полям сохраненные значения
        this.width.setText(width);
        this.height.setText(height);
        areaResult.setText(area);
        perimeterResult.setText(perimeter);
    }
}
