package com.androidlabs.fragment.figures

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.androidlabs.R
import com.androidlabs.data.AppDatabase
import com.androidlabs.data.dao.CalculationsDAO
import com.androidlabs.data.dao.DataDao
import com.androidlabs.databinding.FragmentSquareBinding
import com.androidlabs.provider.MyContentProvider
import com.androidlabs.util.App
import org.decimal4j.util.DoubleRounder

class Square : Fragment() {
    //Главный layout экрана
    private var mainLayout: LinearLayout? = null

    //Поле для ввода и кнопка очистки полей
    private var editSide: EditText? = null
    private var clearFields: ImageButton? = null

    //Поля с выводом результатов рассчёта
    private var areaResult: TextView? = null
    private var perimeterResult: TextView? = null

    //Кнопка подсчёта результатов и записью результатов в бд
    private var calculateAndSaveIntoDB: Button? = null

    //Интерфейсы для работы с классами Calculations и Data
    private var calculationsDAO: CalculationsDAO? = null
    private var dataDao: DataDao? = null
    private var figureId = 0
    private var precision: Int? = null
    private var settings: SharedPreferences? = null
    private var binding: FragmentSquareBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Получаем заголовок из ресурсов
        val square = context!!.getString(R.string.squareTitle)
        // Присваиваем toolbar новый заголовок
        activity!!.title = square
        binding = FragmentSquareBinding.inflate(layoutInflater, container, false)
        val view = binding.getRoot()
        // Инициализация главного layout'а
        mainLayout = binding.mainLayout
        // Инициализация ImageView
        val icon = binding.icon

        // Инициализация бд и интерфейсов для работы с моделями бд
        val db: AppDatabase = App.Companion.getInstance().getDatabase()
        val figureDao = db.figureDao()
        calculationsDAO = db.calculationsDAO()
        dataDao = db.dataDao()
        // Получить айди из таблицы фигур
        figureId = figureDao!!.getIdByName(square)

        // Инициализация настроек
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        // Получаем точность из настроек
        precision = Integer.valueOf(settings.getString("pre", "1")!!)
        val log = context!!.getString(R.string.precisionReturned, precision)
        Log.d(javaClass.simpleName, log)
        addElementsProgrammatically(context)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Слушатель кнопки очистки полей
        clearFields!!.setOnClickListener { v: View? ->
            showLog(R.string.clearFieldsPressed)
            editSide!!.setText("")
            areaResult!!.text = ""
            perimeterResult!!.text = ""
            showLog(R.string.clearFieldsComplete)
        }
        //Слушатель кнопки рассчета результата и записи в бд
        calculateAndSaveIntoDB!!.setOnClickListener { view: View? ->
            showLog(R.string.calculateAndSaveIntoDBPressed)
            val sideValue = java.lang.Double.valueOf(editSide!!.text.toString())
            calculateArea(sideValue)
            calculatePerimeter(sideValue)
            saveIntoDatabase(sideValue)
        }
    }

    override fun onPause() {
        super.onPause()
        //Сохраняем данные полей в настройки
        val prefEditor = settings!!.edit()
        prefEditor.putString("side", editSide!!.text.toString())
        prefEditor.putString("area", areaResult!!.text.toString())
        prefEditor.putString("perimeter", perimeterResult!!.text.toString())
        prefEditor.apply()
    }

    override fun onResume() {
        super.onResume()
        //Получаем данные полей из настроек
        val side = settings!!.getString("side", "0")
        val area = settings!!.getString("area", "0")
        val perimeter = settings!!.getString("perimeter", "0")
        //Присваиваем полям сохраненные значения
        editSide!!.setText(side)
        areaResult!!.text = area
        perimeterResult!!.text = perimeter
    }

    private fun calculateArea(side: Double) {
        // Рассчитываем площадь
        val area = side * side
        // Оставляем кол-во знаков после запятой, равное числу переменной precision
        val preciseArea = DoubleRounder.round(area, precision!!)
        // Присваиваем полю с выводом результата новое значение
        areaResult!!.text = preciseArea.toString()
        showLog(R.string.calculateAreaComplete)
    }

    private fun calculatePerimeter(side: Double) {
        // Рассчитываем периметр
        val perimeter = 4 * side
        // Оставляем кол-во знаков после запятой, равное числу переменной precision
        val precisePerimeter = DoubleRounder.round(perimeter, precision!!)
        // Присваиваем полю с выводом результата новое значение
        perimeterResult!!.text = precisePerimeter.toString()
        showLog(R.string.calculatePerimeterComplete)
    }

    private fun saveIntoDatabase(side: Double) {
        val precisedWidth = 0.toDouble()
        val precisedHeight = 0.toDouble()
        val precisedRadius = 0.toDouble()
        val data_values = ContentValues()
        data_values.put("width", precisedWidth)
        data_values.put("height", precisedHeight)
        data_values.put("side", side)
        data_values.put("radius", precisedRadius)
        val dataUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_DATA, data_values
        )
        val id_data = Integer.valueOf(dataUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Data: $id_data")
        val calculations_values = ContentValues()
        calculations_values.put("figureId", figureId)
        calculations_values.put("dataId", id_data)
        calculations_values.put("area", java.lang.Double.valueOf(areaResult!!.text.toString()))
        calculations_values.put("perimeter", java.lang.Double.valueOf(perimeterResult!!.text.toString()))
        val calculationsUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_CALCULATIONS, calculations_values
        )
        val id_calculations = Integer.valueOf(calculationsUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Calculations: $id_calculations")
    }

    private fun addElementsProgrammatically(context: Context?) {
        //Инициализация layout'а с полем ввода и кнопкой очистки полей
        //Layout с полем для ввода и кнопкой очистки полей
        val editSideAndClearFieldsLayout = LinearLayout(context)
        editSideAndClearFieldsLayout.gravity = Gravity.CENTER
        editSide = EditText(context, null, 0, R.style.squareSide)
        clearFields = ImageButton(context, null, 0, R.style.clearButton)
        clearFields!!.scaleType = ImageView.ScaleType.FIT_XY
        //Параметры layout'а с полем ввода и кнопкой очистки полей
        val editSideAndClearFieldsLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editSideAndClearFieldsLayoutParams.topMargin = 125
        //Параметры поля для ввода
        val editSideParams = LinearLayout.LayoutParams(
                450, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //Параметры кнопки очистки полей
        val clearFieldsParams = LinearLayout.LayoutParams(
                135, 135
        )
        clearFieldsParams.marginStart = 50
        //Присваиваем параметры
        editSideAndClearFieldsLayout.layoutParams = editSideAndClearFieldsLayoutParams
        editSide!!.layoutParams = editSideParams
        clearFields!!.layoutParams = clearFieldsParams
        //Добавляем editSide и clearFields в соответствующий layout
        editSideAndClearFieldsLayout.addView(editSide)
        editSideAndClearFieldsLayout.addView(clearFields)
        //Добавляем Layout в главный layout
        mainLayout!!.addView(editSideAndClearFieldsLayout)


        //Инициализация layout'а с полем ввода и кнопкой очистки полей
        //Layout с полями результатов рассчёта
        val calculationsResults = LinearLayout(context)
        //calculationsResults.setGravity(Gravity.CENTER);
        areaResult = TextView(context, null,
                0, R.style.areaResult)
        perimeterResult = TextView(context, null,
                0, R.style.perimeterResult)

        //Параметры layout'а с полем ввода и кнопкой очистки полей
        val calculationsResultsParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        calculationsResultsParams.marginStart = 30
        calculationsResultsParams.marginEnd = 30
        calculationsResultsParams.topMargin = 125
        //Параметры поля для ввода
        val areaResultParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 66
        )
        areaResultParams.marginEnd = 50
        areaResultParams.weight = 1f

        //Параметры кнопки очистки полей
        val perimeterResultParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 66
        )
        perimeterResultParams.marginStart = 50
        perimeterResultParams.weight = 1f

        //Присваиваем параметры
        calculationsResults.layoutParams = calculationsResultsParams
        areaResult!!.layoutParams = areaResultParams
        perimeterResult!!.layoutParams = perimeterResultParams

        //Добавляем editSide и clearFields в соответствующий layout
        calculationsResults.addView(areaResult)
        calculationsResults.addView(perimeterResult)

        //Добавляем Layout в главный layout
        mainLayout!!.addView(calculationsResults)


        //Инициализация кнопки рассчёта параметров и
        // сохранения результатов в бд
        calculateAndSaveIntoDB = Button(context, null,
                0, R.style.calculateAndSaveIntoDB)
        calculateAndSaveIntoDB!!.gravity = Gravity.CENTER
        //Параметры кнопки подсчёта площади
        val calculateAndSaveIntoDBParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        calculateAndSaveIntoDBParams.topMargin = 150
        calculateAndSaveIntoDBParams.marginStart = 55
        calculateAndSaveIntoDBParams.marginEnd = 55
        calculateAndSaveIntoDBParams.bottomMargin = 55
        //Присваиваем параметры
        calculateAndSaveIntoDB!!.layoutParams = calculateAndSaveIntoDBParams
        //Добавляем кнопку в главный layout
        mainLayout!!.addView(calculateAndSaveIntoDB)
    }

    private fun showLog(messageId: Int) {
        //Подбираем текст из ресурсов по его id
        val message = context!!.getString(messageId)
        //Отображение сообщения в логах
        Log.d(javaClass.simpleName, message)
    }
}