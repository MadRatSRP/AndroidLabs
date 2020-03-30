package com.androidlabs.fragment.figures;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.androidlabs.R;
import com.androidlabs.data.AppDatabase;
import com.androidlabs.data.dao.CalculationsDAO;
import com.androidlabs.data.dao.DataDao;
import com.androidlabs.data.dao.FigureDao;
import com.androidlabs.data.entity.Calculations;
import com.androidlabs.databinding.FragmentRectangleBinding;
import com.androidlabs.provider.MyContentProvider;
import com.androidlabs.util.App;
import com.google.android.material.snackbar.Snackbar;

import org.decimal4j.util.DoubleRounder;

public class Rectangle extends Fragment {
    private CalculationsDAO calculationsDAO;
    private DataDao dataDao;
    private int figureId;

    private Calculations calculations;

    private Integer precision;

    private SharedPreferences settings;

    private FragmentRectangleBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String rectangle = getContext().getString(R.string.rectangleTitle);

        getActivity().setTitle(rectangle);

        binding = FragmentRectangleBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

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
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.setupWidth.setBackgroundResource(R.drawable.backtext);
        binding.setupHeight.setBackgroundResource(R.drawable.backtext);

        binding.clearFields.setBackgroundResource(R.drawable.ic_clear);
        binding.clearFields.setOnClickListener(v -> {
            showLog(R.string.clearFieldsPressed);
            binding.setupWidth.setText("");
            binding.setupHeight.setText("");
            binding.calculatedAreaResult.setText("");
            binding.calculatedPerimeterResult.setText("");
            showLog(R.string.clearFieldsComplete);
        });

        binding.calculateAndSaveIntoDatabase.setOnClickListener(view -> {
            showLog(R.string.calculateAndSaveIntoDBPressed);

            Double width = Double.valueOf(binding.setupWidth.getText().toString());
            Double height = Double.valueOf(binding.setupHeight.getText().toString());

            calculateArea(width, height);
            calculatePerimeter(width, height);
            saveIntoDatabase(width, height);
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        //Сохраняем данные полей в настройки
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putString("width", binding.setupWidth.getText().toString());
        prefEditor.putString("height", binding.setupHeight.getText().toString());
        prefEditor.putString("area", binding.calculatedAreaResult.getText().toString());
        prefEditor.putString("perimeter", binding.calculatedPerimeterResult.getText().toString());
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
        binding.setupWidth.setText(width);
        binding.setupHeight.setText(height);
        binding.calculatedAreaResult.setText(area);
        binding.calculatedPerimeterResult.setText(perimeter);
    }


    private void calculateArea(Double width, Double height) {
        double area = width * height;
        double preciseArea = DoubleRounder.round(area, precision);

        binding.calculatedAreaResult.setText(String.valueOf(preciseArea));
        showLog(R.string.calculateAreaComplete);
    }
    private void calculatePerimeter(Double width, Double height) {
        double perimeter = 2 * (width + height);
        double precisePerimeter = DoubleRounder.round(perimeter, precision);

        binding.calculatedPerimeterResult.setText(String.valueOf(precisePerimeter));
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
        calculations_values.put("area",
                Double.valueOf(binding.calculatedAreaResult.getText().toString()));
        calculations_values.put("perimeter",
                Double.valueOf(binding.calculatedPerimeterResult.getText().toString()));

        Uri calculationsUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_CALCULATIONS, calculations_values
        );
        int id_calculations = Integer.valueOf(calculationsUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Calculations: " + id_calculations);

        showLog(R.string.calculateAndSaveIntoDBComplete);
        showSnack(getView(), R.string.calculateAndSaveIntoDBComplete);
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
}
