package com.androidlabs.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.androidlabs.R
import com.androidlabs.adapter.FiguresAdapter.FiguresViewHolder
import com.androidlabs.model.Figure
import java.util.*

class FiguresAdapter : RecyclerView.Adapter<FiguresViewHolder>() {
    var figures: MutableList<Figure> = ArrayList()
    fun updateFiguresList(new_figures: List<Figure>?) {
        figures.clear()
        figures.addAll(new_figures!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiguresViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_figures, parent, false)
        return FiguresViewHolder(view)
    }

    override fun onBindViewHolder(holder: FiguresViewHolder, position: Int) {
        //holder.figureName.setText(figures.get(position));
        holder.figureName.text = figures[position].figureName

        /*holder.toFigure.setOnClickListener(view -> {
            if (position == 0) moveToFragment(view, R.id.square);
            else if (position == 1) moveToFragment(view, R.id.rectangle);
            else if (position == 2) moveToFragment(view, R.id.circle);
        });*/holder.toFigure.setOnClickListener { view: View? -> moveToFragment(view, figures[position].fragmentId) }

        /*if (position == 0) holder.figureIcon.setImageResource(R.drawable.square);
        else if (position == 1) holder.figureIcon.setImageResource(R.drawable.rectangle);
        else if (position == 2) holder.figureIcon.setImageResource(R.drawable.circle);*/holder.figureIcon.setImageResource(figures[position].drawableId)
    }

    fun moveToFragment(view: View?, fragmentId: Int?) {
        Navigation.findNavController(view!!).navigate(fragmentId!!)
    }

    override fun getItemCount(): Int {
        return figures.size
    }

    inner class FiguresViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val figureName: TextView
        val figureIcon: ImageView
        val toFigure: ImageButton

        init {
            figureName = view.findViewById(R.id.figureName)
            figureIcon = view.findViewById(R.id.figureImageView)
            toFigure = view.findViewById(R.id.figureToFigure)
        }
    }
}