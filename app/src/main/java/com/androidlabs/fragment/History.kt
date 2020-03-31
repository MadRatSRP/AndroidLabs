package com.androidlabs.fragment

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidlabs.R
import com.androidlabs.adapter.HistoryAdapter
import com.androidlabs.data.entity.Calculations
import com.androidlabs.databinding.FragmentHistoryBinding
import com.androidlabs.model.History
import com.androidlabs.provider.MyContentProvider
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class History : Fragment() {
    private var toolbar: Toolbar? = null
    private var addHistory: ImageButton? = null
    private var historyAdapter: HistoryAdapter? = null
    private var spinnerText: String? = null

    private var mBinding: FragmentHistoryBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.historyTitle)

        mBinding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        addHistoryButtonInitialization()

        historyAdapter = HistoryAdapter()
        binding.recyclerView.adapter = historyAdapter
        showLog(R.string.recyclerViewInitialized)
        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            setDataFromCSVFile(context)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            setTestValuesFromCSVFile(context)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                fillRecyclerViewFromDatabase(context)
                binding.swipeRefreshLayout.isRefreshing = false
            }, 500)
        }
        addHistory?.setOnClickListener { view: View? ->
            showLog(R.string.historyAddHistoryClicked)
            showAddHistoryDialog(context)
        }
        binding.clearHistoryList.setOnClickListener { view: View? ->
            showLog(R.string.historyRemoveAllHistoriesClicked)
            showRemoveHistoriesDialog(context)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        toolbar!!.removeView(addHistory)
    }

    private fun addHistoryButtonInitialization() {
        addHistory = ImageButton(context, null, 0, R.style.addHistory)
        toolbar = activity?.findViewById(R.id.toolbar)
        val params = LinearLayout.LayoutParams(85, 85)
        params.marginStart = 175
        addHistory?.layoutParams = params
        toolbar?.addView(addHistory)
    }
    private fun showAddHistoryDialog(context: Context?) {
        // Инициализируем AlertDialog, указываем ему заголовок и описание
        val builder = AlertDialog.Builder(context!!)
        builder.setCancelable(true)
        builder.setTitle(context.getText(R.string.historyAddHistoryTitle))
        builder.setMessage(context.getText(R.string.historyAddHistoryDescription))

        // Создаём главный layout и задаем его положене
        val mainLayout = LinearLayout(context)
        mainLayout.orientation = LinearLayout.VERTICAL

        // Layout выбора типа
        val typeLayout = LinearLayout(context)
        val figureType = TextView(context, null, 0, R.style.figureType)
        val spinner = Spinner(context)

        // Параметры TypeLayout и его элементов
        val typeLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val figureTypeParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        figureTypeParams.weight = 1f
        val spinnerParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        spinnerParams.weight = 1f

        // Получаем массив названий фигур и преобразовываем его в ArrayList<String>
        val bundle: Bundle = context.contentResolver.call(
                MyContentProvider.Companion.URI_CALCULATIONS, "getFiguresNames", null, null)!!
        val spinnerItems = bundle.getStringArray("spinnerItems")
        val spinnerArray = ArrayList(listOf(*spinnerItems))

        // Инициализируем адаптер и присваиваем его спиннеру
        val spinnerAdapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_dropdown_item, spinnerArray
        )
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                spinnerText = spinner.selectedItem.toString()
                Log.d(javaClass.simpleName, spinnerText)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Присваиваем параметры typelayout и его элементам
        typeLayout.layoutParams = typeLayoutParams
        figureType.layoutParams = figureTypeParams
        spinner.layoutParams = spinnerParams
        // Добавляем элементы typelayout в typelayout
        typeLayout.addView(figureType)
        typeLayout.addView(spinner)
        // Добавляем typelayout в главный layout
        mainLayout.addView(typeLayout)

        // Layout ширины и высоты
        val widthAndHeightLayout = LinearLayout(context)
        val width = EditText(context, null, 0, R.style.width)
        val height = EditText(context, null, 0, R.style.height)
        //
        val widthAndHeightLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val widthParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        widthParams.weight = 1f
        widthParams.marginStart = 30
        widthParams.marginEnd = 30
        val heightParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        heightParams.weight = 1f
        heightParams.marginEnd = 30
        heightParams.marginStart = 30
        widthAndHeightLayout.layoutParams = widthAndHeightLayoutParams
        width.layoutParams = widthParams
        height.layoutParams = heightParams
        widthAndHeightLayout.addView(width)
        widthAndHeightLayout.addView(height)
        mainLayout.addView(widthAndHeightLayout)
        val sideAndRadiusLayout = LinearLayout(context)
        val side = EditText(context, null, 0, R.style.squareSide)
        val radius = EditText(context, null, 0, R.style.circleRadius)
        val sideAndRadiusLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        sideAndRadiusLayoutParams.topMargin = 55
        val sideParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        sideParams.weight = 1f
        sideParams.marginStart = 30
        sideParams.marginEnd = 30
        val radiusParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        radiusParams.weight = 1f
        radiusParams.marginStart = 30
        radiusParams.marginEnd = 30
        sideAndRadiusLayout.layoutParams = sideAndRadiusLayoutParams
        side.layoutParams = sideParams
        radius.layoutParams = radiusParams
        sideAndRadiusLayout.addView(side)
        sideAndRadiusLayout.addView(radius)
        mainLayout.addView(sideAndRadiusLayout)
        val areaAndPerimeterLayout = LinearLayout(context)
        val area = EditText(context, null, 0, R.style.areaEditText)
        val perimeter = EditText(context, null, 0, R.style.perimeterEditText)
        //Указываем положение layout'а и подсказки edittext'ам
        val areaAndPerimeterLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        areaAndPerimeterLayoutParams.topMargin = 55
        val areaParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        areaParams.weight = 1f
        areaParams.marginStart = 30
        areaParams.marginEnd = 30
        val perimeterParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        perimeterParams.weight = 1f
        perimeterParams.marginStart = 30
        perimeterParams.marginEnd = 30
        areaAndPerimeterLayout.layoutParams = areaAndPerimeterLayoutParams
        area.layoutParams = areaParams
        perimeter.layoutParams = perimeterParams
        areaAndPerimeterLayout.addView(area)
        areaAndPerimeterLayout.addView(perimeter)
        mainLayout.addView(areaAndPerimeterLayout)

        //Добавляем layout в alertdialog
        builder.setView(mainLayout)

        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive)) { dialog: DialogInterface?, which: Int ->
            newHistory(context, width.text.toString(), height.text.toString(), side.text.toString(),
                    radius.text.toString(), area.text.toString(), perimeter.text.toString())
            showSnack(view, R.string.historyAddHistoryMessage)
            showLog(R.string.historyAddHistoryMessage)
        }
        builder.setNeutralButton(context.getText(R.string.buttonNeutral)) { dialog: DialogInterface?, which: Int -> }
        //Наконец, показываем AlertDialog
        builder.show()
    }

    fun newHistory(context: Context?, width: String?, height: String?, side: String?,
                   radius: String?, area: String?, perimeter: String?) {
        val precisedWidth = checkForNull(width)
        val precisedHeight = checkForNull(height)
        val precisedSide = checkForNull(side)
        val precisedRadius = checkForNull(radius)
        val precisedArea = checkForNull(area)
        val precisedPerimeter = checkForNull(perimeter)
        val data_values = ContentValues()
        data_values.put("width", precisedWidth)
        data_values.put("height", precisedHeight)
        data_values.put("side", precisedSide)
        data_values.put("radius", precisedRadius)
        val dataUri = context!!.contentResolver.insert(
                MyContentProvider.Companion.URI_DATA, data_values
        )
        val id_data = Integer.valueOf(dataUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName, "Новый элемент таблицы Data: $id_data")
        addHistory(getContext(), id_data, precisedArea, precisedPerimeter)
    }

    private fun addHistory(context: Context?, dataId: Int, area: Double, perimeter: Double) {
        val id = historyAdapter!!.itemCount
        historyAdapter!!.addHistory(History(id, dataId,
                spinnerText, area, perimeter))
        val bundle: Bundle = context!!.contentResolver.call(
                MyContentProvider.Companion.URI_CALCULATIONS, "getFigureId", spinnerText, null)!!
        val figureId = bundle.getInt("figureId")
        val calculationsValues = ContentValues()
        //calculationsValues.put("spin", spinnerText);
        calculationsValues.put("figureId", figureId)
        calculationsValues.put("dataId", dataId)
        calculationsValues.put("area", area)
        calculationsValues.put("perimeter", perimeter)
        val calculationsUri = context.contentResolver
                .insert(MyContentProvider.Companion.URI_CALCULATIONS, calculationsValues)
        val id_calculations = Integer.valueOf(calculationsUri!!.lastPathSegment!!)
        Log.d(javaClass.simpleName,
                "Новый элемент таблицы Calculations: $id_calculations")
    }

    fun checkForNull(text: String?): Double {
        val jojo: Double
        return if (TextUtils.isEmpty(text)) {
            jojo = 0.toDouble()
            jojo
        } else {
            jojo = java.lang.Double.valueOf(text!!)
            jojo
        }
        //Log.d(getClass().getSimpleName(), String.valueOf(jojo));
    }

    private fun showRemoveHistoriesDialog(context: Context?) {
        //Инициализируем AlertDialog, указываем ему заголовок и описание
        val builder = AlertDialog.Builder(context!!)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.historyRemoveAllHistoriesTitle))
        builder.setMessage(context.getString(R.string.historyRemoveAllHistoriesDescription))
        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive)) { dialog: DialogInterface?, which: Int ->
            clearHistories()
            Log.d(javaClass.simpleName, context.getString(R.string.historyRemoveAllHistoriesMessage))
        }
        builder.setNeutralButton(context.getText(R.string.buttonNeutral)) { dialog: DialogInterface?, which: Int -> }
        //Показываем AlertDialog
        builder.show()
    }

    @Throws(IOException::class)
    private fun setDataFromCSVFile(context: Context?) {
        //Указываем потоку название файла в папке ресурсов assets
        val file = getContext()!!.assets.open("data.csv")
        //Указываем путь к файлу и кодировку, в которой будем его читать
        val bufferedReader = BufferedReader(
                InputStreamReader(file, "Windows-1251"))
        //Создаём парсер, указываем ему формат csv файла, первую запись как заголовок,
        //разделитель в виде ;, игнорирование заглавности заголовка
        val csvParser = CSVParser(bufferedReader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(';')
                .withIgnoreHeaderCase()
                .withTrim())
        //Проходим циклом по таблице и заносим данные в список histories
        for (csvRecord in csvParser) {
            val width = java.lang.Double.valueOf(csvRecord["width"])
            val height = java.lang.Double.valueOf(csvRecord["height"])
            val side = java.lang.Double.valueOf(csvRecord["side"])
            val radius = java.lang.Double.valueOf(csvRecord["radius"])
            val data_values = ContentValues()
            data_values.put("width", width)
            data_values.put("height", height)
            data_values.put("side", side)
            data_values.put("radius", radius)

            /*Data data = new Data();
            data.width = width;
            data.height = height;
            data.side = side;
            data.radius = radius;*/
            val dataUri = context!!.contentResolver.insert(
                    MyContentProvider.Companion.URI_DATA, data_values
            )
            val id_data = Integer.valueOf(dataUri!!.lastPathSegment!!)
            Log.d(javaClass.simpleName, "Новый элемент таблицы Data: $id_data")
        }
    }

    @Throws(IOException::class)
    private fun setTestValuesFromCSVFile(context: Context?) {
        //Объявляем новый список типа History
        val histories: MutableList<History> = ArrayList()
        //Очищаем список
        histories.clear()
        //Указываем потоку название файла в папке ресурсов assets
        val file = getContext()!!.assets.open("test.csv")
        //Указываем путь к файлу и кодировку, в которой будем его читать
        val bufferedReader = BufferedReader(
                InputStreamReader(file, "Windows-1251"))
        //Создаём парсер, указываем ему формат csv файла, первую запись как заголовок,
        //разделитель в виде ;, игнорирование заглавности заголовка
        val csvParser = CSVParser(bufferedReader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(';')
                .withIgnoreHeaderCase()
                .withTrim())
        var bundle: Bundle

        //Проходим циклом по таблице и заносим данные в список histories
        for (csvRecord in csvParser) {
            val id = Integer.valueOf(csvRecord["id"])
            var dataId = 0
            when (id) {
                1 -> {
                    bundle = context!!.contentResolver.call(
                            MyContentProvider.Companion.URI_DATA, "getIdBySide", 66.toString(), null)!!
                    dataId = bundle.getInt("id")
                }
                2 -> {
                    bundle = context!!.contentResolver.call(
                            MyContentProvider.Companion.URI_DATA, "getIdByRadius", 15.toString(), null
                    )!!
                    dataId = bundle.getInt("id")
                }
                3 -> {
                    bundle = Bundle()
                    bundle.putInt("width", 25)
                    bundle.putInt("height", 33)
                    val new_bundle: Bundle = context!!.contentResolver.call(
                            MyContentProvider.Companion.URI_DATA, "getIdByWidthAndHeight", null, bundle
                    )!!
                    dataId = new_bundle.getInt("id")
                }
            }
            val name = csvRecord["name"]
            val area = java.lang.Double.valueOf(csvRecord["area"])
            val perimeter = java.lang.Double.valueOf(csvRecord["perimeter"])
            histories.add(History(id, dataId, name, area, perimeter))
        }
        //Передаём функции получившийся список
        updateRecyclerView(histories)
    }

    private fun fillRecyclerViewFromDatabase(context: Context?) {
        //Объявляем новый список типа History
        val histories: MutableList<History> = ArrayList()
        //Объявляем список типа Calculations и заполняем его полями из БД
        // List<Calculations> calculations = calculationsDAO.getAll();
        val calculationsList: MutableList<Calculations> = ArrayList()
        val cursor = context!!.contentResolver.query(MyContentProvider.Companion.URI_CALCULATIONS,
                null, null, null, null)

        //if ((cursor.moveToFirst() != null) && cursor.moveToFirst())

        //if cursor.moveToFirst() == null
        while (cursor!!.moveToNext()) {
            while (!cursor.isAfterLast) {
                /*public int id;

            public int figureId;

            public int dataId;

            public double area;

            public double perimeter;*/
                val calculations = Calculations()
                calculations.id = cursor.getInt(cursor.getColumnIndex("id"))
                calculations.figureId = cursor.getInt(cursor.getColumnIndex("figureId"))
                calculations.dataId = cursor.getInt(cursor.getColumnIndex("dataId"))
                calculations.area = cursor.getDouble(cursor.getColumnIndex("area"))
                calculations.perimeter = cursor.getDouble(cursor.getColumnIndex("perimeter"))
                calculationsList.add(calculations)
            }
        }

        //cursor.moveToFirst();
        cursor.close()

        //Очищаем список histories
        histories.clear()
        //Циклом заполняем список histories значениями списка calculations
        for (i in calculationsList.indices) {
            val id = calculationsList[i].figureId

            //Подбираем название фигуры по айди
            val bundle: Bundle = getContext()!!.contentResolver.call(MyContentProvider.Companion.URI_CALCULATIONS,
                    "getFigureName", id.toString(), null)!!
            val name = bundle.getString("name")
            val calculationsId = calculationsList[i].id
            val dataId = calculationsList[i].dataId
            val area = calculationsList[i].area
            val perimeter = calculationsList[i].perimeter
            histories.add(History(calculationsId, dataId, name, area, perimeter))
        }
        //Передаём функции апдейта списка получившийся массив
        updateRecyclerView(histories)
        Log.d(javaClass.simpleName, getContext()!!.getString(R.string.recyclerViewFilled))
    }

    private fun updateRecyclerView(histories: List<History>) {
        historyAdapter?.updateHistoryList(histories)
        binding.recyclerView.adapter = historyAdapter
    }

    private fun clearHistories() {
        historyAdapter!!.clearHistory()
        context!!.contentResolver
                .delete(MyContentProvider.Companion.URI_CALCULATIONS, null, null)
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