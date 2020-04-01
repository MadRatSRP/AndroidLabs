package com.androidlabs.fragment

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidlabs.R
import com.androidlabs.activity.MainActivity
import com.androidlabs.adapter.FiguresAdapter
import com.androidlabs.databinding.FragmentFiguresBinding
import com.androidlabs.model.Figure
import com.androidlabs.provider.MyContentProvider
import com.androidlabs.util.showLogMessage
import java.util.*

class Figures : Fragment() {
    private var figuresAdapter: FiguresAdapter? = null
    private val figures: MutableList<Figure> = ArrayList()

    // ViewBinding variables
    private var mBinding: FragmentFiguresBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbarTitle(R.string.figuresTitle)

        // ViewBinding initialization
        mBinding = FragmentFiguresBinding.inflate(inflater, container, false)
        val view = binding.root

        // Adapter initialization and its assignment to recyclerView
        figuresAdapter = FiguresAdapter()
        binding.recyclerView.adapter = figuresAdapter

        showLogMessage(R.string.recyclerViewInitialized)
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

        showLogMessage(R.string.recyclerViewFilled)
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun removeAllFigures() {
        context?.contentResolver?.delete(
                MyContentProvider.URI_FIGURE,
                null, null)
    }
    private fun insertNewFigure(name: String?) {
        val figureValues = ContentValues()
        figureValues.put("name", name)

        val figureUri = context?.contentResolver?.insert(
                MyContentProvider.URI_FIGURE, figureValues
        )

        val dataId = figureUri?.lastPathSegment?.toInt()
        showLogMessage("Новый элемент таблицы Figure: $dataId")
    }
}