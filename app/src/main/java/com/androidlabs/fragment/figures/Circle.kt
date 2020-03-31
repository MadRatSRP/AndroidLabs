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
import com.androidlabs.databinding.FragmentCircleBinding
import com.androidlabs.provider.MyContentProvider
import com.androidlabs.util.App
import com.google.android.material.snackbar.Snackbar
import org.decimal4j.util.DoubleRounder

class Circle : Fragment() {
    private var calculationsDAO: CalculationsDAO? = null
    private var dataDao: DataDao? = null
    private var figureId = 0
    private var calculations: Calculations? = null
    private var precision: Int? = null
    private var settings: SharedPreferences? = null
    private var binding: FragmentCircleBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val circleTitle = context!!.getString(R.string.circleTitle)
        activity!!.title = circleTitle
        binding = FragmentCircleBinding.inflate(layoutInflater, container, false)
        val view = binding.getRoot()
        val db: AppDatabase = App.Companion.getInstance().getDatabase()
        val figureDao = db.figureDao()
        calculationsDAO = db.calculationsDAO()
        dataDao = db.dataDao()
        figureId = figureDao!!.getIdByName(circleTitle)
        calculations = Calculations()
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        precision = Integer.valueOf(settings.getString("pre", "1")!!)
        val log = context!!.getString(R.string.precisionReturned, precision)
        Log.d(javaClass.simpleName, log)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.clearValues.setOnClickListener({ v ->
            showLog(R.string.clearFieldsPressed)
            binding.radiusValue.setText("")
            binding.areaResult.setText("")
            binding.perimeterResult.setText("")
            showLog(R.string.clearFieldsComplete)
        })
        binding!!.calculateAndSaveIntoDatabase.setOnClickListener { view: View? ->
            showLog(R.string.calculateAndSaveIntoDBPressed)
            val calculatedRadius: Double = java.lang.Double.valueOf(binding.radiusValue.getText().toString())
            calculateArea(calculatedRadius)
            calculatePerimeter(calculatedRadius)
            saveIntoDatabase(calculatedRadius)
        }
    }

    override fun onPause() {
        super.onPause()
        val prefEditor = settings!!.edit()
        prefEditor.putString("radius", binding.radiusValue.getText().toString())
        prefEditor.putString("area", binding.areaResult.getText().toString())
        prefEditor.putString("perimeter", binding.perimeterResult.getText().toString())
        prefEditor.apply()
    }

    override fun onResume() {
        super.onResume()

        //Получаем данные полей из настроек
        val radius = settings!!.getString("radius", "0")
        val area = settings!!.getString("area", "0")
        val perimeter = settings!!.getString("perimeter", "0")

        //Присваиваем полям сохраненные значения
        binding.radiusValue.setText(radius)
        binding.areaResult.setText(area)
        binding.perimeterResult.setText(perimeter)
    }

    private fun calculateArea(radius: Double) {
        val area = 3.14 * radius * radius
        val preciseArea = DoubleRounder.round(area, precision!!)
        binding.areaResult.setText(preciseArea.toString())
        showLog(R.string.calculateAreaComplete)
    }

    private fun calculatePerimeter(radius: Double) {
        val perimeter = 2 * radius * 3.14
        val precisePerimeter = DoubleRounder.round(perimeter, precision!!)
        binding.perimeterResult.setText(precisePerimeter.toString())
        showLog(R.string.calculatePerimeterComplete)
    }

    private fun saveIntoDatabase(radius: Double) {
        val precisedWidth = 0.toDouble()
        val precisedHeight = 0.toDouble()
        val precisedSide = 0.toDouble()
        val data_values = ContentValues()
        data_values.put("width", precisedWidth)
        data_values.put("height", precisedHeight)
        data_values.put("side", precisedSide)
        data_values.put("radius", radius)
        val dataUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_DATA, data_values
        )
        val id_data = Integer.valueOf(dataUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Data: $dataUri")
        val calculations_values = ContentValues()
        calculations_values.put("figureId", figureId)
        calculations_values.put("dataId", id_data)
        calculations_values.put("area", java.lang.Double.valueOf(binding.areaResult.getText().toString()))
        calculations_values.put("perimeter", java.lang.Double.valueOf(binding.perimeterResult.getText().toString()))
        val calculationsUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_CALCULATIONS, calculations_values
        )
        val id_calculations = Integer.valueOf(calculationsUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Calculations: $calculationsUri")
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