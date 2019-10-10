package com.androidlabs.fragment.figures;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.decimal4j.util.DoubleRounder;

public class Square extends Fragment {
    //Главный layout экрана
    private LinearLayout squareLayout;

    //Поле для ввода и кнопка очистки полей
    private EditText editSide;
    private ImageButton clearFields;

    //Поля с выводом результатов рассчёта
    private TextView areaResult;
    private TextView perimeterResult;

    //Кнопка подсчёта результатов и записью результатов в бд
    private Button calculateAndSaveIntoDB;

    //Интерфейсы для работы с классами Calculations и Data
    private CalculationsDAO calculationsDAO;
    private DataDao dataDao;
    private int figureId;

    private Integer precision;

    private SharedPreferences settings;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Слушатель кнопки очистки полей
        clearFields.setOnClickListener(v -> {
            showLog(R.string.clearFieldsPressed);
            editSide.setText("");
            areaResult.setText("");
            perimeterResult.setText("");
            showLog(R.string.clearFieldsComplete);
        });
        //Слушатель кнопки рассчета результата и записи в бд
        calculateAndSaveIntoDB.setOnClickListener(view -> {
            showLog(R.string.calculateAndSaveIntoDBPressed);

            calculateArea(Double.valueOf(editSide.getText().toString()));
            calculatePerimeter(Double.valueOf(editSide.getText().toString()));
            saveIntoDatabase(Double.valueOf(editSide.getText().toString()));
        });
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Получаем заголовок из ресурсов
        String square = getContext().getString(R.string.squareTitle);
        // Присваиваем toolbar новый заголовок
        getActivity().setTitle(square);
        View view = inflater.inflate(R.layout.fragment_square, container, false);
        // Инициализация главного layout'а
        squareLayout = view.findViewById(R.id.squareLinearLayout);
        // Инициализация ImageView
        ImageView icon = view.findViewById(R.id.squareIcon);

        // Инициализация бд и интерфейсов для работы с моделями бд
        AppDatabase db = App.getInstance().getDatabase();
        FigureDao figureDao = db.figureDao();
        calculationsDAO = db.calculationsDAO();
        dataDao = db.dataDao();
        // Получить айди из таблицы фигур
        figureId = figureDao.getIdByName(square);

        // Инициализация настроек
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Получаем точность из настроек
        precision = Integer.valueOf(settings.getString("pre", "1"));

        String log = getContext().getString(R.string.precisionReturned, precision);
        Log.d(getClass().getSimpleName(), log);

        addElementsProgrammatically(getContext());

        return view;
    }

    private void calculateArea(Double side) {
        // Рассчитываем площадь
        double area = side * side;
        // Оставляем кол-во знаков после запятой, равное числу переменной precision
        double preciseArea = DoubleRounder.round(area, precision);
        // Присваиваем полю с выводом результата новое значение
        areaResult.setText(String.valueOf(preciseArea));
        showLog(R.string.calculateAreaComplete);
    }
    private void calculatePerimeter(Double side) {
        // Рассчитываем периметр
        double perimeter = 4 * side;
        // Оставляем кол-во знаков после запятой, равное числу переменной precision
        double precisePerimeter = DoubleRounder.round(perimeter, precision);
        // Присваиваем полю с выводом результата новое значение
        perimeterResult.setText(String.valueOf(precisePerimeter));
        showLog(R.string.calculatePerimeterComplete);
    }
    private void saveIntoDatabase(Double side) {
        double precisedWidth = (double) 0;
        double precisedHeight = (double) 0;
        double precisedSide = side;
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
    }
    private void addElementsProgrammatically(Context context) {
        //Инициализация layout'а с полем ввода и кнопкой очистки полей
        //Layout с полем для ввода и кнопкой очистки полей
        LinearLayout editSideAndClearFieldsLayout = new LinearLayout(context);
        editSideAndClearFieldsLayout.setGravity(Gravity.CENTER);
        editSide = new EditText(context, null, 0, R.style.squareSide);
        clearFields = new ImageButton(context, null, 0, R.style.clearButton);
        clearFields.setScaleType(ImageView.ScaleType.FIT_XY);
        //Параметры layout'а с полем ввода и кнопкой очистки полей
        LinearLayout.LayoutParams editSideAndClearFieldsLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        editSideAndClearFieldsLayoutParams.topMargin = 125;
        //Параметры поля для ввода
        LinearLayout.LayoutParams editSideParams = new LinearLayout.LayoutParams(
                450, ViewGroup.LayoutParams.WRAP_CONTENT
        );

        //Параметры кнопки очистки полей
        LinearLayout.LayoutParams clearFieldsParams = new LinearLayout.LayoutParams(
                135, 135
        );
        clearFieldsParams.setMarginStart(50);
        //Присваиваем параметры
        editSideAndClearFieldsLayout.setLayoutParams(editSideAndClearFieldsLayoutParams);
        editSide.setLayoutParams(editSideParams);
        clearFields.setLayoutParams(clearFieldsParams);
        //Добавляем editSide и clearFields в соответствующий layout
        editSideAndClearFieldsLayout.addView(editSide);
        editSideAndClearFieldsLayout.addView(clearFields);
        //Добавляем Layout в главный layout
        squareLayout.addView(editSideAndClearFieldsLayout);



        //Инициализация layout'а с полем ввода и кнопкой очистки полей
        //Layout с полями результатов рассчёта
        LinearLayout calculationsResults = new LinearLayout(context);
        //calculationsResults.setGravity(Gravity.CENTER);

        areaResult = new TextView(context, null,
                0, R.style.areaResult);
        perimeterResult = new TextView(context, null,
                0, R.style.perimeterResult);

        //Параметры layout'а с полем ввода и кнопкой очистки полей
        LinearLayout.LayoutParams calculationsResultsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        calculationsResultsParams.setMarginStart(30);
        calculationsResultsParams.setMarginEnd(30);
        calculationsResultsParams.topMargin = 125;
        //Параметры поля для ввода
        LinearLayout.LayoutParams areaResultParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 66
        );
        areaResultParams.setMarginEnd(50);
        areaResultParams.weight = 1;

        //Параметры кнопки очистки полей
        LinearLayout.LayoutParams perimeterResultParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 66
        );
        perimeterResultParams.setMarginStart(50);
        perimeterResultParams.weight = 1;

        //Присваиваем параметры
        calculationsResults.setLayoutParams(calculationsResultsParams);
        areaResult.setLayoutParams(areaResultParams);
        perimeterResult.setLayoutParams(perimeterResultParams);

        //Добавляем editSide и clearFields в соответствующий layout
        calculationsResults.addView(areaResult);
        calculationsResults.addView(perimeterResult);

        //Добавляем Layout в главный layout
        squareLayout.addView(calculationsResults);



        //Инициализация кнопки рассчёта параметров и
        // сохранения результатов в бд
        calculateAndSaveIntoDB = new Button(context, null,
                0, R.style.calculateAndSaveIntoDB);
        calculateAndSaveIntoDB.setGravity(Gravity.CENTER);
        //Параметры кнопки подсчёта площади
        LinearLayout.LayoutParams calculateAndSaveIntoDBParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        calculateAndSaveIntoDBParams.topMargin = 150;
        calculateAndSaveIntoDBParams.setMarginStart(55);
        calculateAndSaveIntoDBParams.setMarginEnd(55);
        calculateAndSaveIntoDBParams.bottomMargin = 55;
        //Присваиваем параметры
        calculateAndSaveIntoDB.setLayoutParams(calculateAndSaveIntoDBParams);
        //Добавляем кнопку в главный layout
        squareLayout.addView(calculateAndSaveIntoDB);
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
        prefEditor.putString("side", editSide.getText().toString());
        prefEditor.putString("area", areaResult.getText().toString());
        prefEditor.putString("perimeter", perimeterResult.getText().toString());
        prefEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Получаем данные полей из настроек
        String side = settings.getString("side", "0");
        String area = settings.getString("area", "0");
        String perimeter = settings.getString("perimeter", "0");
        //Присваиваем полям сохраненные значения
        editSide.setText(side);
        areaResult.setText(area);
        perimeterResult.setText(perimeter);
    }
}
