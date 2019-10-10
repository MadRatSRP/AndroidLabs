package com.androidlabs.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidlabs.R;
import com.androidlabs.adapter.HistoryAdapter;
import com.androidlabs.provider.MyContentProvider;
import com.androidlabs.util.App;
import com.androidlabs.data.AppDatabase;
import com.androidlabs.data.dao.CalculationsDAO;
import com.androidlabs.data.dao.DataDao;
import com.androidlabs.data.dao.FigureDao;
import com.androidlabs.data.entity.Calculations;
import com.androidlabs.data.entity.Data;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class History extends Fragment {

    private Toolbar toolbar;
    private ImageButton addHistory;

    private HistoryAdapter historyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView historyRecyclerView;

    private Button removeAllRows;

    private String spinnerText;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            setDataFromCSVFile(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            setTestValuesFromCSVFile(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed((() -> {
                fillRecyclerViewFromDatabase(getContext());
                swipeRefreshLayout.setRefreshing(false);
            }), 500);
        });

        addHistory.setOnClickListener(view -> {
            showLog(R.string.historyAddHistoryClicked);
            showAddHistoryDialog(getContext());
        });

        removeAllRows.setOnClickListener(view -> {
            showLog(R.string.historyRemoveAllHistoriesClicked);
            showRemoveHistoriesDialog(getContext());
        });
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(R.string.historyTitle);
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        addHistory = new ImageButton(getContext(), null, 0, R.style.addHistory);
        toolbar = getActivity().findViewById(R.id.toolbar);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
        params.setMarginStart(175);
        addHistory.setLayoutParams(params);
        toolbar.addView(addHistory);

        swipeRefreshLayout = view.findViewById(R.id.historySwipeRefreshLayout);
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        removeAllRows = view.findViewById(R.id.historyRemoveAllRows);

        historyAdapter = new HistoryAdapter();
        historyRecyclerView.setAdapter(historyAdapter);

        showLog(R.string.recyclerViewInitialized);
        return view;
    }

    private void showAddHistoryDialog(Context context) {
        // Инициализируем AlertDialog, указываем ему заголовок и описание
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getText(R.string.historyAddHistoryTitle));
        builder.setMessage(context.getText(R.string.historyAddHistoryDescription));

        // Создаём главный layout и задаем его положене
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Layout выбора типа
        LinearLayout typeLayout = new LinearLayout(context);
        TextView figureType = new TextView(context, null, 0, R.style.figureType);
        Spinner spinner = new Spinner(context);

        // Параметры TypeLayout и его элементов
        LinearLayout.LayoutParams typeLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams figureTypeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        figureTypeParams.weight = 1;
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.weight = 1;

        // Получаем массив названий фигур и преобразовываем его в ArrayList<String>
        Bundle bundle = context.getContentResolver().call(
                MyContentProvider.URI_CALCULATIONS, "getFiguresNames", null, null);
        String[] spinnerItems = bundle.getStringArray("spinnerItems");


        ArrayList<String> spinnerArray = new ArrayList<>(Arrays.asList(spinnerItems));

        // Инициализируем адаптер и присваиваем его спиннеру
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_dropdown_item, spinnerArray
        );
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerText = spinner.getSelectedItem().toString();
                Log.d(getClass().getSimpleName(), spinnerText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Присваиваем параметры typelayout и его элементам
        typeLayout.setLayoutParams(typeLayoutParams);
        figureType.setLayoutParams(figureTypeParams);
        spinner.setLayoutParams(spinnerParams);
        // Добавляем элементы typelayout в typelayout
        typeLayout.addView(figureType);
        typeLayout.addView(spinner);
        // Добавляем typelayout в главный layout
        mainLayout.addView(typeLayout);

        // Layout ширины и высоты
        LinearLayout widthAndHeightLayout = new LinearLayout(context);
        EditText width = new EditText(context, null, 0, R.style.width);
        EditText height = new EditText(context, null, 0, R.style.height);
        //
        LinearLayout.LayoutParams widthAndHeightLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams widthParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        widthParams.weight = 1;
        widthParams.setMarginStart(30);
        widthParams.setMarginEnd(30);
        LinearLayout.LayoutParams heightParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        heightParams.weight = 1;
        heightParams.setMarginEnd(30);
        heightParams.setMarginStart(30);

        widthAndHeightLayout.setLayoutParams(widthAndHeightLayoutParams);
        width.setLayoutParams(widthParams);
        height.setLayoutParams(heightParams);

        widthAndHeightLayout.addView(width);
        widthAndHeightLayout.addView(height);

        mainLayout.addView(widthAndHeightLayout);

        LinearLayout sideAndRadiusLayout = new LinearLayout(context);
        EditText side = new EditText(context, null, 0, R.style.squareSide);
        EditText radius = new EditText(context, null, 0, R.style.circleRadius);

        LinearLayout.LayoutParams sideAndRadiusLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sideAndRadiusLayoutParams.topMargin = 55;
        LinearLayout.LayoutParams sideParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sideParams.weight = 1;
        sideParams.setMarginStart(30);
        sideParams.setMarginEnd(30);
        LinearLayout.LayoutParams radiusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        radiusParams.weight = 1;
        radiusParams.setMarginStart(30);
        radiusParams.setMarginEnd(30);

        sideAndRadiusLayout.setLayoutParams(sideAndRadiusLayoutParams);
        side.setLayoutParams(sideParams);
        radius.setLayoutParams(radiusParams);

        sideAndRadiusLayout.addView(side);
        sideAndRadiusLayout.addView(radius);

        mainLayout.addView(sideAndRadiusLayout);


        LinearLayout areaAndPerimeterLayout = new LinearLayout(context);
        EditText area = new EditText(context, null, 0, R.style.areaEditText);
        EditText perimeter = new EditText(context, null, 0, R.style.perimeterEditText);
        //Указываем положение layout'а и подсказки edittext'ам

        LinearLayout.LayoutParams areaAndPerimeterLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        areaAndPerimeterLayoutParams.topMargin = 55;
        LinearLayout.LayoutParams areaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        areaParams.weight = 1;
        areaParams.setMarginStart(30);
        areaParams.setMarginEnd(30);
        LinearLayout.LayoutParams perimeterParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        perimeterParams.weight = 1;
        perimeterParams.setMarginStart(30);
        perimeterParams.setMarginEnd(30);

        areaAndPerimeterLayout.setLayoutParams(areaAndPerimeterLayoutParams);
        area.setLayoutParams(areaParams);
        perimeter.setLayoutParams(perimeterParams);

        areaAndPerimeterLayout.addView(area);
        areaAndPerimeterLayout.addView(perimeter);

        mainLayout.addView(areaAndPerimeterLayout);

        //Добавляем layout в alertdialog
        builder.setView(mainLayout);

        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive), (dialog, which) -> {

            newHistory(context, width.getText().toString(), height.getText().toString(), side.getText().toString(),
                    radius.getText().toString(), area.getText().toString(), perimeter.getText().toString());

            showSnack(getView(), R.string.historyAddHistoryMessage);
            showLog(R.string.historyAddHistoryMessage);
        });
        builder.setNeutralButton(context.getText(R.string.buttonNeutral), (dialog, which) -> { });
        //Наконец, показываем AlertDialog
        builder.show();
    }
    void newHistory(Context context, String width, String height, String side,
                    String radius, String area, String perimeter) {
        Double precisedWidth = checkForNull(width);
        Double precisedHeight = checkForNull(height);
        Double precisedSide = checkForNull(side);
        Double precisedRadius = checkForNull(radius);
        Double precisedArea = checkForNull(area);
        Double precisedPerimeter = checkForNull(perimeter);

        ContentValues data_values = new ContentValues();
        data_values.put("width", precisedWidth);
        data_values.put("height", precisedHeight);
        data_values.put("side", precisedSide);
        data_values.put("radius", precisedRadius);

        Uri dataUri = context.getContentResolver().insert(
                MyContentProvider.URI_DATA, data_values
        );
        int id_data = Integer.valueOf(dataUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Data: " + id_data);

        addHistory(getContext(), id_data, precisedArea, precisedPerimeter);
    }

    private void addHistory(Context context, int dataId, double area, double perimeter) {
        int id = historyAdapter.getItemCount();

        historyAdapter.addHistory(new com.androidlabs.model.History(id, dataId,
                spinnerText, area, perimeter));

        Bundle bundle = context.getContentResolver().call(
                MyContentProvider.URI_CALCULATIONS, "getFigureId", spinnerText, null);

        int figureId = bundle.getInt("figureId");

        ContentValues calculationsValues = new ContentValues();
        //calculationsValues.put("spin", spinnerText);
        calculationsValues.put("figureId", figureId);
        calculationsValues.put("dataId", dataId);
        calculationsValues.put("area", area);
        calculationsValues.put("perimeter", perimeter);

        Uri calculationsUri = context.getContentResolver()
                .insert(MyContentProvider.URI_CALCULATIONS, calculationsValues);

        int id_calculations = Integer.valueOf(calculationsUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(),
                "Новый элемент таблицы Calculations: " + id_calculations);
    }

    Double checkForNull(String text) {
        double jojo;

        if (TextUtils.isEmpty(text)) {
            jojo = (double) 0;
            return jojo;
        } else {
            jojo = Double.valueOf(text);
            return jojo;
        }
        //Log.d(getClass().getSimpleName(), String.valueOf(jojo));
    }

    private void showRemoveHistoriesDialog(Context context) {
        //Инициализируем AlertDialog, указываем ему заголовок и описание
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.historyRemoveAllHistoriesTitle));
        builder.setMessage(context.getString(R.string.historyRemoveAllHistoriesDescription));
        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive), (dialog, which) -> {
            clearHistories();
            Log.d(getClass().getSimpleName(), context.getString(R.string.historyRemoveAllHistoriesMessage));
        });
        builder.setNeutralButton(context.getText(R.string.buttonNeutral), (dialog, which) -> { });
        //Показываем AlertDialog
        builder.show();
    }

    private void setDataFromCSVFile(Context context) throws IOException {
        //Указываем потоку название файла в папке ресурсов assets
        InputStream file = getContext().getAssets().open("data.csv");
        //Указываем путь к файлу и кодировку, в которой будем его читать
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(file, "Windows-1251"));
        //Создаём парсер, указываем ему формат csv файла, первую запись как заголовок,
        //разделитель в виде ;, игнорирование заглавности заголовка
        CSVParser csvParser = new CSVParser(bufferedReader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(';')
                .withIgnoreHeaderCase()
                .withTrim());
        //Проходим циклом по таблице и заносим данные в список histories
        for (CSVRecord csvRecord:csvParser) {
            Double width = Double.valueOf(csvRecord.get("width"));
            Double height = Double.valueOf(csvRecord.get("height"));
            Double side = Double.valueOf(csvRecord.get("side"));
            Double radius = Double.valueOf(csvRecord.get("radius"));

            ContentValues data_values = new ContentValues();
            data_values.put("width", width);
            data_values.put("height", height);
            data_values.put("side", side);
            data_values.put("radius", radius);

            /*Data data = new Data();
            data.width = width;
            data.height = height;
            data.side = side;
            data.radius = radius;*/

            Uri dataUri = context.getContentResolver().insert(
                    MyContentProvider.URI_DATA, data_values
            );
            int id_data = Integer.valueOf(dataUri.getLastPathSegment());
            Log.d(getClass().getSimpleName(), "Новый элемент таблицы Data: " + id_data);
        }
    }
    private void setTestValuesFromCSVFile(Context context) throws IOException {
        //Объявляем новый список типа History
        List<com.androidlabs.model.History> histories = new ArrayList<>();
        //Очищаем список
        histories.clear();
        //Указываем потоку название файла в папке ресурсов assets
        InputStream file = getContext().getAssets().open("test.csv");
        //Указываем путь к файлу и кодировку, в которой будем его читать
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(file, "Windows-1251"));
        //Создаём парсер, указываем ему формат csv файла, первую запись как заголовок,
        //разделитель в виде ;, игнорирование заглавности заголовка
        CSVParser csvParser = new CSVParser(bufferedReader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(';')
                .withIgnoreHeaderCase()
                .withTrim());
        Bundle bundle;

        //Проходим циклом по таблице и заносим данные в список histories
        for (CSVRecord csvRecord:csvParser) {


            int id = Integer.valueOf(csvRecord.get("id"));

            int dataId = 0;
            switch(id) {
                case 1: {
                    bundle = context.getContentResolver().call(
                            MyContentProvider.URI_DATA, "getIdBySide",
                            String.valueOf(66), null);

                    dataId = bundle.getInt("id");
                    break;
                }
                case 2: {
                    bundle = context.getContentResolver().call(
                            MyContentProvider.URI_DATA, "getIdByRadius",
                            String.valueOf(15), null
                    );
                    dataId = bundle.getInt("id");
                    break;
                }
                case 3: {
                    bundle = new Bundle();
                    bundle.putInt("width", 25);
                    bundle.putInt("height", 33);

                    Bundle new_bundle = context.getContentResolver().call(
                            MyContentProvider.URI_DATA, "getIdByWidthAndHeight", null, bundle
                    );

                    dataId = new_bundle.getInt("id");
                    break;
                }
            }

            String name = csvRecord.get("name");
            Double area = Double.valueOf(csvRecord.get("area"));
            Double perimeter = Double.valueOf(csvRecord.get("perimeter"));

            histories.add(new com.androidlabs.model.History(id, dataId, name, area, perimeter));
        }
        //Передаём функции получившийся список
        updateRecyclerView(histories);
    }



    private void fillRecyclerViewFromDatabase(Context context) {
        //Объявляем новый список типа History
        List<com.androidlabs.model.History> histories = new ArrayList<>();
        //Объявляем список типа Calculations и заполняем его полями из БД
       // List<Calculations> calculations = calculationsDAO.getAll();

        List<Calculations> calculationsList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(MyContentProvider.URI_CALCULATIONS,
                null, null, null, null);

        //if ((cursor.moveToFirst() != null) && cursor.moveToFirst())

        //if cursor.moveToFirst() == null

        while(cursor.moveToNext()) {
            while (!cursor.isAfterLast()) {
            /*public int id;

            public int figureId;

            public int dataId;

            public double area;

            public double perimeter;*/

                Calculations calculations = new Calculations();
                calculations.id = cursor.getInt(cursor.getColumnIndex("id"));
                calculations.figureId = cursor.getInt(cursor.getColumnIndex("figureId"));
                calculations.dataId = cursor.getInt(cursor.getColumnIndex("dataId"));
                calculations.area = cursor.getDouble(cursor.getColumnIndex("area"));
                calculations.perimeter = cursor.getDouble(cursor.getColumnIndex("perimeter"));

                calculationsList.add(calculations);
            }
        }

        //cursor.moveToFirst();

        cursor.close();

        //Очищаем список histories
        histories.clear();
        //Циклом заполняем список histories значениями списка calculations
        for (int i = 0; i < calculationsList.size(); i++) {
            int id = calculationsList.get(i).figureId;

            //Подбираем название фигуры по айди

            Bundle bundle = getContext().getContentResolver().call(MyContentProvider.URI_CALCULATIONS,
                    "getFigureName", String.valueOf(id), null);

            String name = bundle.getString("name");

            int calculationsId = calculationsList.get(i).id;
            int dataId = calculationsList.get(i).dataId;
            Double area = calculationsList.get(i).area;
            Double perimeter = calculationsList.get(i).perimeter;

            histories.add(new com.androidlabs.model.History(calculationsId, dataId, name, area, perimeter));
        }
        //Передаём функции апдейта списка получившийся массив
        updateRecyclerView(histories);
        Log.d(getClass().getSimpleName(), getContext().getString(R.string.recyclerViewFilled));
    }
    private void updateRecyclerView(List<com.androidlabs.model.History> histories) {
        historyAdapter.updateHistoryList(histories);
        historyRecyclerView.setAdapter(historyAdapter);
    }


    private void clearHistories() {
        historyAdapter.clearHistory();

        getContext().getContentResolver()
                .delete(MyContentProvider.URI_CALCULATIONS, null, null);
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
    public void onDestroy() {
        super.onDestroy();
        toolbar.removeView(addHistory);
    }
}
