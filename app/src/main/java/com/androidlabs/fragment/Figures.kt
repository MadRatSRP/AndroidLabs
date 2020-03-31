package com.androidlabs.fragment

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.androidlabs.R
import com.androidlabs.adapter.FiguresAdapter
import com.androidlabs.databinding.FragmentFiguresBinding
import com.androidlabs.model.Figure
import com.androidlabs.provider.MyContentProvider
import java.util.*

class Figures : Fragment() {
    private var figuresAdapter: FiguresAdapter? = null
    private val figures: MutableList<Figure> = ArrayList()

    private var mBinding: FragmentFiguresBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.figuresTitle)

        // ViewBinding initialization
        mBinding = FragmentFiguresBinding.inflate(inflater, container, false)
        val view = binding.root

        // Adapter initialization and its assignment to recyclerView
        figuresAdapter = FiguresAdapter()
        binding.recyclerView.adapter = figuresAdapter
        context?.getString(R.string.recyclerViewInitialized)?.let { Log.d(javaClass.simpleName, it) }
        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val square = context!!.getString(R.string.squareTitle)
        val rectangle = context!!.getString(R.string.rectangleTitle)
        val circle = context!!.getString(R.string.circleTitle)

        figures.clear()
        figures.add(Figure(R.drawable.square, square, R.id.square))
        figures.add(Figure(R.drawable.rectangle, rectangle, R.id.rectangle))
        figures.add(Figure(R.drawable.circle, circle, R.id.circle))

        removeAllFigures()
        insertNewFigure(square)
        insertNewFigure(rectangle)
        insertNewFigure(circle)

        figuresAdapter?.updateFiguresList(figures)
        binding.recyclerView.adapter = figuresAdapter
        Log.d(javaClass.simpleName, context!!.getString(R.string.recyclerViewFilled))
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun removeAllFigures() {
        context?.contentResolver?.delete(MyContentProvider.Companion.URI_FIGURE,
                null, null)
    }
    private fun insertNewFigure(name: String?) {
        val figure_values = ContentValues()
        figure_values.put("name", name)
        val figureUri = context?.contentResolver?.insert(
                MyContentProvider.Companion.URI_FIGURE, figure_values
        )
        val id_data = figureUri?.lastPathSegment?.let {
            Integer.valueOf(it)
        }
        Log.d(javaClass.simpleName, "Новый элемент таблицы Figure: $id_data")
    }
}