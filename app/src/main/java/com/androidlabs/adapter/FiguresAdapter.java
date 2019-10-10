package com.androidlabs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.androidlabs.R;
import com.androidlabs.model.Figure;

import java.util.ArrayList;
import java.util.List;

public class FiguresAdapter extends RecyclerView.Adapter<FiguresAdapter.FiguresViewHolder> {
    List<Figure> figures = new ArrayList<>();

    public void updateFiguresList(List<Figure> new_figures) {
        figures.clear();
        figures.addAll(new_figures);
        this.notifyDataSetChanged();
    }

    @Override
    public FiguresViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_figures, parent, false);
        return new FiguresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FiguresAdapter.FiguresViewHolder holder, int position) {
        //holder.figureName.setText(figures.get(position));
        holder.figureName.setText(figures.get(position).getFigureName());

        /*holder.toFigure.setOnClickListener(view -> {
            if (position == 0) moveToFragment(view, R.id.square);
            else if (position == 1) moveToFragment(view, R.id.rectangle);
            else if (position == 2) moveToFragment(view, R.id.circle);
        });*/
        holder.toFigure.setOnClickListener(view -> {
            moveToFragment(view, figures.get(position).getFragmentId());
        });

        /*if (position == 0) holder.figureIcon.setImageResource(R.drawable.square);
        else if (position == 1) holder.figureIcon.setImageResource(R.drawable.rectangle);
        else if (position == 2) holder.figureIcon.setImageResource(R.drawable.circle);*/
        holder.figureIcon.setImageResource(figures.get(position).getDrawableId());

    }

    void moveToFragment(View view, Integer fragmentId) {
        Navigation.findNavController(view).navigate(fragmentId);
    }

    @Override
    public int getItemCount() {
        return figures.size();
    }

    public class FiguresViewHolder extends RecyclerView.ViewHolder{
        private TextView figureName;
        private ImageView figureIcon;
        private ImageButton toFigure;

        public FiguresViewHolder(final View view) {
            super(view);
            figureName = view.findViewById(R.id.figureName);
            figureIcon = view.findViewById(R.id.figureImageView);
            toFigure = view.findViewById(R.id.figureToFigure);
        }
    }
}
