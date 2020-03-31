package com.androidlabs.fragment.figures

import android.content.ContentValues
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.androidlabs.R
import com.androidlabs.data.AppDatabase
import com.androidlabs.data.dao.CalculationsDAO
import com.androidlabs.data.dao.DataDao
import com.androidlabs.data.entity.Calculations
import com.androidlabs.databinding.FragmentRectangleBinding
import com.androidlabs.provider.MyContentProvider
import com.androidlabs.util.App
import com.google.android.material.snackbar.Snackbar
import org.decimal4j.util.DoubleRounder

class Rectangle : Fragment() {
    private var calculationsDAO: CalculationsDAO? = null
    private var dataDao: DataDao? = null
    private var figureId = 0
    private var calculations: Calculations? = null
    private var precision: Int? = null
    private var settings: SharedPreferences? = null
    private var binding: FragmentRectangleBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rectangle = context!!.getString(R.string.rectangleTitle)
        activity!!.title = rectangle
        binding = FragmentRectangleBinding.inflate(layoutInflater, container, false)
        val view = binding.getRoot()
        val db: AppDatabase = App.Companion.getInstance().getDatabase()
        val figureDao = db.figureDao()
        calculationsDAO = db.calculationsDAO()
        dataDao = db.dataDao()
        figureId = figureDao!!.getIdByName(rectangle)
        calculations = Calculations()
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        precision = Integer.valueOf(settings.getString("pre", "1")!!)
        val log = context!!.getString(R.string.precisionReturned, precision)
        Log.d(javaClass.simpleName, log)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding!!.setupWidth.setBackgroundResource(R.drawable.backtext)
        binding!!.setupHeight.setBackgroundResource(R.drawable.backtext)
        binding!!.clearFields.setBackgroundResource(R.drawable.ic_clear)
        binding!!.clearFields.setOnClickListener { v: View? ->
            showLog(R.string.clearFieldsPressed)
            binding!!.setupWidth.setText("")
            binding!!.setupHeight.setText("")
            binding!!.calculatedAreaResult.text = ""
            binding!!.calculatedPerimeterResult.text = ""
            showLog(R.string.clearFieldsComplete)
        }
        binding!!.calculateAndSaveIntoDatabase.setOnClickListener { view: View? ->
            showLog(R.string.calculateAndSaveIntoDBPressed)
            val width = java.lang.Double.valueOf(binding!!.setupWidth.text.toString())
            val height = java.lang.Double.valueOf(binding!!.setupHeight.text.toString())
            calculateArea(width, height)
            calculatePerimeter(width, height)
            saveIntoDatabase(width, height)
        }
    }

    override fun onPause() {
        super.onPause()
        //Сохраняем данные полей в настройки
        val prefEditor = settings!!.edit()
        prefEditor.putString("width", binding!!.setupWidth.text.toString())
        prefEditor.putString("height", binding!!.setupHeight.text.toString())
        prefEditor.putString("area", binding!!.calculatedAreaResult.text.toString())
        prefEditor.putString("perimeter", binding!!.calculatedPerimeterResult.text.toString())
        prefEditor.apply()
    }

    override fun onResume() {
        super.onResume()

        //Получаем данные полей из настроек
        val width = settings!!.getString("width", "0")
        val height = settings!!.getString("height", "0")
        val area = settings!!.getString("area", "0")
        val perimeter = settings!!.getString("perimeter", "0")

        //Присваиваем полям сохраненные значения
        binding!!.setupWidth.setText(width)
        binding!!.setupHeight.setText(height)
        binding!!.calculatedAreaResult.text = area
        binding!!.calculatedPerimeterResult.text = perimeter
    }

    private fun calculateArea(width: Double, height: Double) {
        val area = width * height
        val preciseArea = DoubleRounder.round(area, precision!!)
        binding!!.calculatedAreaResult.text = preciseArea.toString()
        showLog(R.string.calculateAreaComplete)
    }

    private fun calculatePerimeter(width: Double, height: Double) {
        val perimeter = 2 * (width + height)
        val precisePerimeter = DoubleRounder.round(perimeter, precision!!)
        binding!!.calculatedPerimeterResult.text = precisePerimeter.toString()
        showLog(R.string.calculatePerimeterComplete)
    }

    private fun saveIntoDatabase(width: Double, height: Double) {
        val precisedSide = 0.toDouble()
        val precisedRadius = 0.toDouble()
        val data_values = ContentValues()
        data_values.put("width", width)
        data_values.put("height", height)
        data_values.put("side", precisedSide)
        data_values.put("radius", precisedRadius)
        val dataUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_DATA, data_values
        )
        val id_data = Integer.valueOf(dataUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Data: $id_data")
        val calculations_values = ContentValues()
        calculations_values.put("figureId", figureId)
        calculations_values.put("dataId", id_data)
        calculations_values.put("area",
                java.lang.Double.valueOf(binding!!.calculatedAreaResult.text.toString()))
        calculations_values.put("perimeter",
                java.lang.Double.valueOf(binding!!.calculatedPerimeterResult.text.toString()))
        val calculationsUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_CALCULATIONS, calculations_values
        )
        val id_calculations = Integer.valueOf(calculationsUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Calculations: $id_calculations")
        showLog(R.string.calculateAndSaveIntoDBComplete)
        showSnack(view, R.string.calculateAndSaveIntoDBComplete)
    }

    private fun showSnack(view: View?, messageId: Int) {
        //Подбираем текст из ресурсов по его id
        val message = context!!.getString(messageId)
        //Отображаем снэк
        Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showLog(messageId: Int) {
        //Подбираем текст из ресурсов по его id
        val message = context!!.getString(messageId)
        //Отображение сообщения в логах
        Log.d(javaClass.simpleName, message)
    }
}