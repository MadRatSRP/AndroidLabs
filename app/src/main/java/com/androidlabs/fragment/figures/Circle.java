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
import com.androidlabs.databinding.FragmentCircleBinding;
import com.androidlabs.provider.MyContentProvider;
import com.androidlabs.util.App;
import com.google.android.material.snackbar.Snackbar;

import org.decimal4j.util.DoubleRounder;

public class Circle extends Fragment {
    private CalculationsDAO calculationsDAO;
    private DataDao dataDao;

    private int figureId;

    private Calculations calculations;

    private Integer precision;

    private SharedPreferences settings;

    private FragmentCircleBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String circleTitle = getContext().getString(R.string.circleTitle);

        getActivity().setTitle(circleTitle);

        binding = FragmentCircleBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

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
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.clearValues.setOnClickListener(v -> {
            showLog(R.string.clearFieldsPressed);
            binding.radiusValue.setText("");
            binding.areaResult.setText("");
            binding.perimeterResult.setText("");
            showLog(R.string.clearFieldsComplete);
        });
        binding.calculateAndSaveIntoDatabase.setOnClickListener(view -> {
            showLog(R.string.calculateAndSaveIntoDBPressed);
            Double calculatedRadius = Double.valueOf(binding.radiusValue.getText().toString());
            calculateArea(calculatedRadius);
            calculatePerimeter(calculatedRadius);
            saveIntoDatabase(calculatedRadius);
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putString("radius", binding.radiusValue.getText().toString());
        prefEditor.putString("area", binding.areaResult.getText().toString());
        prefEditor.putString("perimeter", binding.perimeterResult.getText().toString());
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
        binding.radiusValue.setText(radius);
        binding.areaResult.setText(area);
        binding.perimeterResult.setText(perimeter);
    }

    private void calculateArea(Double radius) {
        double area = 3.14 * radius * radius;
        double preciseArea = DoubleRounder.round(area, precision);

        binding.areaResult.setText(String.valueOf(preciseArea));
        showLog(R.string.calculateAreaComplete);
    }
    private void calculatePerimeter(Double radius) {
        double perimeter = 2 * radius * 3.14;
        double precisePerimeter = DoubleRounder.round(perimeter, precision);

        binding.perimeterResult.setText(String.valueOf(precisePerimeter));
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
        calculations_values.put("area", Double.valueOf(binding.areaResult.getText().toString()));
        calculations_values.put("perimeter", Double.valueOf(binding.perimeterResult.getText().toString()));

        Uri calculationsUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_CALCULATIONS, calculations_values
        );
        int id_calculations = Integer.valueOf(calculationsUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Calculations: " + calculationsUri.toString());

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
