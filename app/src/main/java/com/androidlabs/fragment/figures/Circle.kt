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
import com.androidlabs.databinding.FragmentCircleBinding
import com.androidlabs.util.showLogMessage
import com.androidlabs.util.showSnackMessage
import org.decimal4j.util.DoubleRounder

class Circle : Fragment() {
    private var calculationsDAO: CalculationsDAO? = null
    private var dataDao: DataDao? = null
    private var figureId = 0
    private var calculations: Calculations? = null

    // Precision
    private var precision: Int = 0

    // AndroidX Preferences
    private lateinit var settings: SharedPreferences

    // ViewBinding variables
    private var mBinding: FragmentCircleBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting up fragment's toolbar title
        val toolbarTitle = context?.getString(R.string.circleTitle)
        toolbarTitle?.let { (activity as MainActivity).setToolbarTitle(it) }

        // ViewBinding initialization
        mBinding = FragmentCircleBinding.inflate(layoutInflater, container, false)
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

        // Showing precision in log
        context?.getString(R.string.precisionReturned, precision)?.let { showLogMessage(it) }
        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.clearFields.setOnClickListener {
            showLogMessage(R.string.clearFieldsPressed)

            val clearText = ""

            binding.setupRadius.setText(clearText)
            binding.calculatedAreaResult.text = clearText
            binding.calculatedPerimeterResult.text = clearText

            showLogMessage(R.string.clearFieldsComplete)
        }
        binding.calculateAndSaveIntoDatabase.setOnClickListener { view: View? ->
            showLogMessage(R.string.calculateAndSaveIntoDBPressed)

            val calculatedRadius = binding.setupRadius.text.toString().toDouble()

            calculateArea(calculatedRadius)
            calculatePerimeter(calculatedRadius)
            saveIntoDatabase(calculatedRadius)
        }
    }
    override fun onPause() {
        super.onPause()

        val prefEditor = settings.edit()
        prefEditor?.putString("radius", binding.setupRadius.text.toString())
        prefEditor?.putString("area", binding.calculatedAreaResult.text.toString())
        prefEditor?.putString("perimeter", binding.calculatedPerimeterResult.text.toString())
        prefEditor?.apply()
    }

    override fun onResume() {
        super.onResume()

        //Получаем данные полей из настроек
        val radius = settings.getString("radius", "0")
        val area = settings.getString("area", "0")
        val perimeter = settings.getString("perimeter", "0")

        //Присваиваем полям сохраненные значения
        binding.setupRadius.setText(radius)
        binding.calculatedAreaResult.text = area
        binding.calculatedPerimeterResult.text = perimeter
    }

    private fun calculateArea(radius: Double) {
        val area = 3.14 * radius * radius
        val preciseArea = DoubleRounder.round(area, precision)
        binding.calculatedAreaResult.text = preciseArea.toString()
        showLogMessage(R.string.calculateAreaComplete)
    }

    private fun calculatePerimeter(radius: Double) {
        val perimeter = 2 * radius * 3.14
        val precisePerimeter = DoubleRounder.round(perimeter, precision)
        binding.calculatedPerimeterResult.text = precisePerimeter.toString()
        showLogMessage(R.string.calculatePerimeterComplete)
    }

    private fun saveIntoDatabase(radius: Double) {
        val precisedWidth = 0.toDouble()
        val precisedHeight = 0.toDouble()
        val precisedSide = 0.toDouble()
        val dataValues = ContentValues()

        dataValues.put("width", precisedWidth)
        dataValues.put("height", precisedHeight)
        dataValues.put("side", precisedSide)
        dataValues.put("radius", radius)

        val dataUri = context?.contentResolver?.insert(
                MyContentProvider.Companion.URI_DATA, dataValues
        )
        val dataId = dataUri?.lastPathSegment?.let { Integer.valueOf(it) }
        Log.d(javaClass.simpleName, "Новый элемент таблицы Data: $dataUri")

        val calculationsValues = ContentValues()
        calculationsValues.put("figureId", figureId)
        calculationsValues.put("dataId", dataId)
        calculationsValues.put("area", binding.calculatedAreaResult.text.toString().toDouble())
        calculationsValues.put("perimeter", binding.calculatedPerimeterResult.text.toString().toDouble())
        val calculationsUri = context?.contentResolver?.insert(
                MyContentProvider.Companion.URI_CALCULATIONS, calculationsValues
        )

        showLogMessage("Новый элемент таблицы Calculations: " +
                "${calculationsUri?.lastPathSegment?.toInt()}")
        showLogMessage(R.string.calculateAndSaveIntoDBComplete)
        showSnackMessage(R.string.calculateAndSaveIntoDBComplete)
    }
}