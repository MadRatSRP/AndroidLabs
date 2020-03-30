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

public class Circle extends Fragment {
    private EditText radius;
    private TextView areaResult;
    private TextView perimeterResult;

    //Кнопка очистки полей
    private ImageButton clearFields;
    //Кнопка вычисления результата и сохранения в бд
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

        clearFields.setOnClickListener(v -> {
            showLog(R.string.clearFieldsPressed);
            radius.setText("");
            areaResult.setText("");
            perimeterResult.setText("");
            showLog(R.string.clearFieldsComplete);
        });
        calculateAndSaveIntoDB.setOnClickListener(view -> {
            showLog(R.string.calculateAndSaveIntoDBPressed);
            calculateArea(Double.valueOf(radius.getText().toString()));
            calculatePerimeter(Double.valueOf(radius.getText().toString()));
            saveIntoDatabase(Double.valueOf(radius.getText().toString()));
        });
    }

    private void calculateArea(Double radius) {
        double area = 3.14 * radius * radius;
        double preciseArea = DoubleRounder.round(area, precision);

        areaResult.setText(String.valueOf(preciseArea));
        showLog(R.string.calculateAreaComplete);
    }
    private void calculatePerimeter(Double radius) {
        double perimeter = 2 * radius * 3.14;
        double precisePerimeter = DoubleRounder.round(perimeter, precision);

        perimeterResult.setText(String.valueOf(precisePerimeter));
        showLog(R.string.calculatePerimeterComplete);
    }
    private void saveIntoDatabase(Double radius) {
        double precisedWidth = (double) 0;
        double precisedHeight = (double) 0;
        double precisedSide = (double) 0;
        double precisedRadius = radius;

        ContentValues data_values = new ContentValues();
        data_values.put("width", precisedWidth);
        data_values.put("height", precisedHeight);
        data_values.put("side", precisedSide);
        data_values.put("radius", precisedRadius);

        Uri dataUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_DATA, data_values
        );
        int id_data = Integer.valueOf(dataUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Data: " + dataUri.toString());

        ContentValues calculations_values = new ContentValues();
        calculations_values.put("figureId", this.figureId);
        calculations_values.put("dataId", id_data);
        calculations_values.put("area", Double.valueOf(areaResult.getText().toString()));
        calculations_values.put("perimeter", Double.valueOf(perimeterResult.getText().toString()));

        Uri calculationsUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_CALCULATIONS, calculations_values
        );
        int id_calculations = Integer.valueOf(calculationsUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Calculations: " + calculationsUri.toString());

        showLog(R.string.calculateAndSaveIntoDBComplete);
        showSnack(getView(), R.string.calculateAndSaveIntoDBComplete);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String circleTitle = getContext().getString(R.string.circleTitle);

        getActivity().setTitle(circleTitle);
        View view = inflater.inflate(R.layout.fragment_circle, container, false);

        AppDatabase db = App.getInstance().getDatabase();
        FigureDao figureDao = db.figureDao();
        calculationsDAO = db.calculationsDAO();
        dataDao = db.dataDao();

        figureId = figureDao.getIdByName(circleTitle);
        calculations = new Calculations();

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        precision = Integer.valueOf(settings.getString("pre", "1"));

        String log = getContext().getString(R.string.precisionReturned, precision);
        Log.d(getClass().getSimpleName(), log);

        radius = view.findViewById(R.id.circleRadius);
        areaResult = view.findViewById(R.id.circleAreaResult);
        perimeterResult = view.findViewById(R.id.circlePerimeterResult);

        clearFields = view.findViewById(R.id.circleClearFields);
        calculateAndSaveIntoDB = view.findViewById(R.id.circleSaveIntoDatabase);
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
        //SharedPreferences settings;

        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putString("radius", radius.getText().toString());
        prefEditor.putString("area", areaResult.getText().toString());
        prefEditor.putString("perimeter", perimeterResult.getText().toString());
        prefEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Получаем данные полей из настроек
        String radius = settings.getString("radius", "0");
        String area = settings.getString("area", "0");
        String perimeter = settings.getString("perimeter", "0");

        //Присваиваем полям сохраненные значения
        this.radius.setText(radius);
        areaResult.setText(area);
        perimeterResult.setText(perimeter);
    }
}
