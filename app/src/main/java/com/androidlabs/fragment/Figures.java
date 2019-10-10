package com.androidlabs.fragment;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.androidlabs.R;
import com.androidlabs.adapter.FiguresAdapter;
import com.androidlabs.model.Figure;
import com.androidlabs.provider.MyContentProvider;
import com.androidlabs.util.App;
import com.androidlabs.data.AppDatabase;
import com.androidlabs.data.dao.FigureDao;

import java.util.ArrayList;
import java.util.List;

public class Figures extends Fragment {
    private FiguresAdapter figuresAdapter;
    private RecyclerView figuresRecyclerView;
    private List<Figure> figures = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String square = getContext().getString(R.string.squareTitle);
        String rectangle = getContext().getString(R.string.rectangleTitle);
        String circle = getContext().getString(R.string.circleTitle);

        figures.clear();
        figures.add(new Figure(R.drawable.square, square, R.id.square));
        figures.add(new Figure(R.drawable.rectangle, rectangle, R.id.rectangle));
        figures.add(new Figure(R.drawable.circle, circle, R.id.circle));

        removeAllFigures();

        insertNewFigure(square);
        insertNewFigure(rectangle);
        insertNewFigure(circle);

        figuresAdapter.updateFiguresList(figures);
        figuresRecyclerView.setAdapter(figuresAdapter);
        Log.d(getClass().getSimpleName(), getContext().getString(R.string.recyclerViewFilled));
    }

    void removeAllFigures() {
        getContext().getContentResolver().delete(MyContentProvider.URI_FIGURE, null, null);
    }

    void insertNewFigure(String name) {


        ContentValues figure_values = new ContentValues();

        figure_values.put("name", name);
        Uri figureUri = getContext().getContentResolver().insert(
                MyContentProvider.URI_FIGURE, figure_values
        );
        int id_data = Integer.valueOf(figureUri.getLastPathSegment());
        Log.d(getClass().getSimpleName(), "Новый элемент таблицы Figure: " + id_data);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(R.string.figuresTitle);
        View view = inflater.inflate(R.layout.fragment_figures, container, false);

        figuresRecyclerView = view.findViewById(R.id.figuresRecyclerView);

        figuresAdapter = new FiguresAdapter();
        figuresRecyclerView.setAdapter(figuresAdapter);
        Log.d(getClass().getSimpleName(), getContext().getString(R.string.recyclerViewInitialized));
        return view;
    }
}
