package com.androidlabs.adapter;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidlabs.R;
import com.androidlabs.data.dao.FigureDao;
import com.androidlabs.data.entity.Calculations;
import com.androidlabs.model.History;
import com.androidlabs.provider.MyContentProvider;
import com.androidlabs.util.App;
import com.androidlabs.data.AppDatabase;
import com.androidlabs.data.dao.CalculationsDAO;
import com.androidlabs.data.dao.DataDao;
import com.androidlabs.data.entity.Data;

import org.decimal4j.util.DoubleRounder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryAdapter
        extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private CalculationsDAO calculationsDAO;
    DataDao dataDao;
    private FigureDao figureDao;

    private SharedPreferences sharedPreferences;

    String spinnerText;



    private ArrayList<History> histories = new ArrayList<>();

    public void clearHistory() {
        histories.clear();
        notifyDataSetChanged();
    }
    public void addHistory(History history) {
        histories.add(history);
        notifyDataSetChanged();

    }
    public void updateHistoryList(List<History> new_histories) {
        histories.clear();
        histories.addAll(new_histories);
        this.notifyDataSetChanged();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        //Получаем разрядность
        int precision = Integer.valueOf(sharedPreferences.getString("pre", "1"));

        int dataId = histories.get(position).getDataId();

        //Data data = dataDao.getDataById(dataId);

        Context context = holder.width.getContext();

        Bundle bundle = context.getContentResolver().call(MyContentProvider.URI_CALCULATIONS, "getDataByDataId",
                String.valueOf(dataId), null);
        Data data = (Data) bundle.getSerializable("data");


        double precisedWidth = DoubleRounder.round(data.width, precision);
        double precisedHeight = DoubleRounder.round(data.height, precision);
        double precisedSide = DoubleRounder.round(data.side, precision);
        double precisedRadius = DoubleRounder.round(data.radius, precision);

        holder.width.setText(String.valueOf(precisedWidth));
        holder.height.setText(String.valueOf(precisedHeight));
        holder.side.setText(String.valueOf(precisedSide));
        holder.radius.setText(String.valueOf(precisedRadius));

        holder.figureName.setText(histories.get(position).getFigureName());

        double precisedArea = DoubleRounder.round(histories.get(position).getArea(), precision);
        holder.figureArea.setText(String.valueOf(precisedArea));

        double precisedPerimeter = DoubleRounder.round(histories.get(position).getPerimeter(), precision);
        holder.figurePerimeter.setText(String.valueOf(precisedPerimeter));

        holder.removeHistory.setOnClickListener(view -> {
            showLog(view.getContext(),
                    R.string.historyRemoveHistoryClicked);
            removeHistoryDialog(view.getContext(), position,
                    histories.get(position).getId());
        });

        holder.updateHistory.setOnClickListener(view -> {
            showLog(view.getContext(),
                    R.string.historyUpdateHistoryClicked);
            /*updateHistoryDialog(view.getContext(), position);*/
        });
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    //Инициализируем поля
    public class HistoryViewHolder extends RecyclerView.ViewHolder{
        private TextView figureId;
        private TextView figureName;
        private TextView figureArea;
        private TextView figurePerimeter;
        private TextView width;
        private TextView height;
        private TextView side;
        private TextView radius;

        private ImageButton removeHistory;
        private ImageButton updateHistory;

        public HistoryViewHolder(final View view) {
            super(view);
            figureName = view.findViewById(R.id.historyFigureName);
            figureArea = view.findViewById(R.id.historyFigureArea);
            figurePerimeter = view.findViewById(R.id.historyFigurePerimeter );

            removeHistory = view.findViewById(R.id.historyRemoveHistory);
            updateHistory = view.findViewById(R.id.historyUpdateHistory);

            width = view.findViewById(R.id.widthValue);
            height = view.findViewById(R.id.heightValue);
            side = view.findViewById(R.id.sideValue);
            radius = view.findViewById(R.id.radiusValue);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        }
    }
    //Диалог удаления истории
    private void removeHistoryDialog(Context context, int position, int id) {
        //Инициализируем AlertDialog, указываем ему заголовок и описание
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.historyRemoveHistoryTitle));
        builder.setMessage(context.getString(R.string.historyRemoveHistoryDescription));
        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive), (dialog, which) -> {
            removeHistory(context, position, id);
            Log.d(getClass().getSimpleName(), context.getString(R.string.historyRemoveHistoryMessage));
        });
        builder.setNeutralButton(context.getText(R.string.buttonNeutral), (dialog, which) -> { });
        //Показываем AlertDialog
        builder.show();
    }
    //Функция удаления истории из списка
    private void removeHistory(Context context, int position, int id) {
        //calculationsDAO.deleteById(id);

        Uri uri = ContentUris.withAppendedId(MyContentProvider.URI_CALCULATIONS,(long) id);
        //context.getContentResolver().delete()
        context.getContentResolver().delete(uri, String.valueOf(id), null);


        histories.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, histories.size());
    }
    // Диалог апдейта истории
    private void updateHistoryDialog(Context context, int position) {
        // Инициализируем AlertDialog, указываем ему заголовок и описание
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getText(R.string.historyUpdateHistoryTitle));
        builder.setMessage(context.getText(R.string.historyUpdateHistoryDescription));

        // Главный layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
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
        layout.addView(typeLayout);
        // Layout периметра и площади
        LinearLayout areaAndPerimeter = new LinearLayout(context);
        EditText area = new EditText(context);
        EditText perimeter = new EditText(context);
        // Параметры areaAndPerimeter
        LinearLayout.LayoutParams areaAndPerimeterParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams areaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        areaParams.weight = 1;
        LinearLayout.LayoutParams perimeterParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        perimeterParams.weight = 1;
        // Присваиваем area и parameter значения
        area.setText(String.valueOf(histories.get(position).getArea()));
        perimeter.setText(String.valueOf(histories.get(position).getPerimeter()));
        // Присваиваем параметры areaAndParameter и его элементам
        areaAndPerimeter.setLayoutParams(areaAndPerimeterParams);
        area.setLayoutParams(areaParams);
        perimeter.setLayoutParams(perimeterParams);
        // Добавляем элементы в areaAndPerimeter
        areaAndPerimeter.addView(area);
        areaAndPerimeter.addView(perimeter);

        layout.addView(areaAndPerimeter);

        //Добавляем layout в alertdialog
        builder.setView(layout);
       // builder.setView(areaAndPerimeter);

        bundle = context.getContentResolver().call(
                MyContentProvider.URI_CALCULATIONS, "getFigureId", spinnerText, null) ;
        int figureId = bundle.getInt("figureId");

        double precisedArea = Double.valueOf(area.getText().toString());
        double precisedPerimeter = Double.valueOf(perimeter.getText().toString());

        History history = new History(histories.get(position).getId(), histories.get(position).getDataId(),
                spinnerText, precisedArea, precisedPerimeter);

        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive), (dialog, which) -> {
            updateHistory(context, position, history, figureId);
            showLog(context, R.string.historyUpdateHistoryMessage);
        });
        builder.setNeutralButton(context.getText(R.string.buttonNeutral), (dialog, which) -> { });
        //Наконец, показываем AlertDialog
        builder.show();
    }
    // Update
    private void updateHistory(Context context, int position, History history, int figureId) {
        int id = history.getId();
        int dataId = history.getDataId();
       // int new_figureId = figureId;
        double area = history.getArea();
        double perimeter = history.getPerimeter();

        ContentValues calculations_values = new ContentValues();
        calculations_values.put("id", id);
        calculations_values.put("dataId", dataId);
        calculations_values.put("figureId", figureId);
        calculations_values.put("area", area);
        calculations_values.put("perimeter", perimeter);

        int rowId = context.getContentResolver()
                .update(MyContentProvider.URI_CALCULATIONS, calculations_values,
                        null, null);
        Log.d(getClass().getSimpleName(), String.valueOf(rowId));

        //Calculations calculations = new Calculations();
       // calculations.id = history.getId();
       // calculations.dataId =
       // calculations.figureId =
      //  calculations.area =
      //  calculations.perimeter =

        //calculationsDAO.update(calculations);

        histories.set(position, history);
        notifyItemChanged(position);
    }
    private void showLog(Context context, Integer messageId) {
        //Подбираем текст из ресурсов по его id
        String message = context.getString(messageId);
        //Отображение сообщения в логах
        Log.d(getClass().getSimpleName(), message);
    }
}
