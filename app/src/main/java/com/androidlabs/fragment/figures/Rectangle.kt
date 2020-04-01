package com.androidlabs.fragment.figures

import android.content.ContentValues
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.androidlabs.R
import com.androidlabs.activity.MainActivity
import com.androidlabs.data.AppDatabase
import com.androidlabs.data.dao.CalculationsDAO
import com.androidlabs.data.dao.DataDao
import com.androidlabs.data.entity.Calculations
import com.androidlabs.databinding.FragmentRectangleBinding
import com.androidlabs.provider.MyContentProvider
import com.androidlabs.util.App
import com.androidlabs.util.showLogMessage
import com.androidlabs.util.showSnackMessage
import org.decimal4j.util.DoubleRounder

class Rectangle : Fragment() {
    private var calculationsDAO: CalculationsDAO? = null
    private var dataDao: DataDao? = null
    private var figureId = 0
    private var calculations: Calculations? = null

    // Precision
    private var precision: Int = 0

    // AndroidX Preferences
    private lateinit var settings: SharedPreferences

    // ViewBinding variables
    private var mBinding: FragmentRectangleBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting up new fragment's toolbar title
        val toolbarTitle = context?.getString(R.string.rectangleTitle)
        toolbarTitle?.let { (activity as MainActivity).setToolbarTitle(it) }

        mBinding = FragmentRectangleBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        val db: AppDatabase = App.instance?.database!!
        val figureDao = db.figureDao()
        calculationsDAO = db.calculationsDAO()
        dataDao = db.dataDao()
        figureId = figureDao.getIdByName(toolbarTitle)
        calculations = Calculations()

        // Settings initialization
        settings = PreferenceManager.getDefaultSharedPreferences(context)

        // Precision initialization
        precision = Integer.valueOf(settings.getString("pre", "1")!!)

        context?.getString(R.string.precisionReturned, precision)?.let { showLogMessage(it) }
        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setupWidth.setBackgroundResource(R.drawable.backtext)
        binding.setupHeight.setBackgroundResource(R.drawable.backtext)
        binding.clearFields.setBackgroundResource(R.drawable.ic_clear)
        binding.clearFields.setOnClickListener {
            showLogMessage(R.string.clearFieldsPressed)

            val clearText = ""

            binding.setupWidth.setText(clearText)
            binding.setupHeight.setText(clearText)
            binding.calculatedAreaResult.text = clearText
            binding.calculatedPerimeterResult.text = clearText

            showLogMessage(R.string.clearFieldsComplete)
        }
        binding.calculateAndSaveIntoDatabase.setOnClickListener {
            showLogMessage(R.string.calculateAndSaveIntoDBPressed)

            val width = binding.setupWidth.text.toString().toDouble()
            val height = binding.setupHeight.text.toString().toDouble()

            calculateArea(width, height)
            calculatePerimeter(width, height)
            saveIntoDatabase(width, height)
        }
    }
    override fun onPause() {
        super.onPause()

        //Сохраняем данные полей в настройки
        val prefEditor = settings.edit()

        prefEditor.putString("width", binding.setupWidth.text.toString())
        prefEditor.putString("height", binding.setupHeight.text.toString())
        prefEditor.putString("area", binding.calculatedAreaResult.text.toString())
        prefEditor.putString("perimeter", binding.calculatedPerimeterResult.text.toString())
        prefEditor.apply()
    }
    override fun onResume() {
        super.onResume()

        //Получаем данные полей из настроек
        val width = settings.getString("width", "0")
        val height = settings.getString("height", "0")
        val area = settings.getString("area", "0")
        val perimeter = settings.getString("perimeter", "0")

        //Присваиваем полям сохраненные значения
        binding.setupWidth.setText(width)
        binding.setupHeight.setText(height)
        binding.calculatedAreaResult.text = area
        binding.calculatedPerimeterResult.text = perimeter
    }

    private fun calculateArea(width: Double, height: Double) {
        val area = width * height
        val preciseArea = DoubleRounder.round(area, precision)
        binding.calculatedAreaResult.text = preciseArea.toString()
        showLogMessage(R.string.calculateAreaComplete)
    }

    private fun calculatePerimeter(width: Double, height: Double) {
        val perimeter = 2 * (width + height)
        val precisePerimeter = DoubleRounder.round(perimeter, precision)
        binding.calculatedPerimeterResult.text = precisePerimeter.toString()
        showLogMessage(R.string.calculatePerimeterComplete)
    }

    private fun saveIntoDatabase(width: Double, height: Double) {
        val precisedSide = 0.toDouble()
        val precisedRadius = 0.toDouble()
        val dataValues = ContentValues()

        dataValues.put("width", width)
        dataValues.put("height", height)
        dataValues.put("side", precisedSide)
        dataValues.put("radius", precisedRadius)

        val dataUri = context?.contentResolver?.insert(
                MyContentProvider.Companion.URI_DATA, dataValues
        )
        val dataId = dataUri?.lastPathSegment?.toInt()
        showLogMessage("Новый элемент таблицы Data: $dataId")

        val calculationsValues = ContentValues()
        calculationsValues.put("figureId", figureId)
        calculationsValues.put("dataId", dataId)
        calculationsValues.put("area",
                binding.calculatedAreaResult.text.toString().toDouble())
        calculationsValues.put("perimeter",
                binding.calculatedPerimeterResult.text.toString().toDouble())

        val calculationsUri = context?.contentResolver?.insert(
                MyContentProvider.Companion.URI_CALCULATIONS, calculationsValues
        )

        val calculationsId = calculationsUri?.lastPathSegment?.toInt()

        showLogMessage("Новый элемент таблицы Calculations: $calculationsId")
        showLogMessage(R.string.calculateAndSaveIntoDBComplete)
        showSnackMessage(R.string.calculateAndSaveIntoDBComplete)
    }
}