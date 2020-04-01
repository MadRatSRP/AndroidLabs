package com.androidlabs.adapter

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.recyclerview.widget.RecyclerView
import com.androidlabs.R
import com.androidlabs.adapter.HistoryAdapter.HistoryViewHolder
import com.androidlabs.data.dao.CalculationsDAO
import com.androidlabs.data.dao.DataDao
import com.androidlabs.data.dao.FigureDao
import com.androidlabs.data.entity.Data
import com.androidlabs.model.History
import com.androidlabs.provider.MyContentProvider
import org.decimal4j.util.DoubleRounder
import java.util.*

class HistoryAdapter : RecyclerView.Adapter<HistoryViewHolder>() {
    private val calculationsDAO: CalculationsDAO? = null

    var dataDao: DataDao? = null

    private val figureDao: FigureDao? = null

    lateinit var sharedPreferences: SharedPreferences

    var spinnerText: String? = null

    private val histories = ArrayList<History>()

    fun clearHistoryList() {
        histories.clear()
        notifyDataSetChanged()
    }
    fun addHistory(history: History) {
        histories.add(history)
        notifyDataSetChanged()
    }
    fun updateHistoryList(new_histories: List<History>?) {
        histories.clear()
        histories.addAll(new_histories!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        //Получаем разрядность
        val precision = Integer.valueOf(sharedPreferences.getString("pre", "1")!!)
        val dataId = histories[position].dataId

        //Data data = dataDao.getDataById(dataId);
        val context = holder.width.context
        val bundle: Bundle = context.contentResolver.call(MyContentProvider.Companion.URI_CALCULATIONS, "getDataByDataId", dataId.toString(), null)!!
        val data = bundle.getSerializable("data") as Data?
        val precisedWidth = DoubleRounder.round(data!!.width!!, precision)
        val precisedHeight = DoubleRounder.round(data.height!!, precision)
        val precisedSide = DoubleRounder.round(data.side!!, precision)
        val precisedRadius = DoubleRounder.round(data.radius!!, precision)
        holder.width.text = precisedWidth.toString()
        holder.height.text = precisedHeight.toString()
        holder.side.text = precisedSide.toString()
        holder.radius.text = precisedRadius.toString()
        holder.figureName.text = histories[position].figureName
        val precisedArea = DoubleRounder.round(histories[position].area, precision)
        holder.figureArea.text = precisedArea.toString()
        val precisedPerimeter = DoubleRounder.round(histories[position].perimeter, precision)
        holder.figurePerimeter.text = precisedPerimeter.toString()
        holder.removeHistory.setOnClickListener { view: View ->
            showLog(view.context,
                    R.string.historyRemoveHistoryClicked)
            removeHistoryDialog(view.context, position,
                    histories[position].id)
        }
        holder.updateHistory.setOnClickListener { view: View ->
            showLog(view.context,
                    R.string.historyUpdateHistoryClicked)
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    //Инициализируем поля
    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val figureId: TextView? = null
        val figureName: TextView
        val figureArea: TextView
        val figurePerimeter: TextView
        val width: TextView
        val height: TextView
        val side: TextView
        val radius: TextView
        val removeHistory: ImageButton
        val updateHistory: ImageButton

        init {
            figureName = view.findViewById(R.id.historyFigureName)
            figureArea = view.findViewById(R.id.historyFigureArea)
            figurePerimeter = view.findViewById(R.id.historyFigurePerimeter)
            removeHistory = view.findViewById(R.id.historyRemoveHistory)
            updateHistory = view.findViewById(R.id.historyUpdateHistory)
            width = view.findViewById(R.id.widthValue)
            height = view.findViewById(R.id.heightValue)
            side = view.findViewById(R.id.sideValue)
            radius = view.findViewById(R.id.setupRadius)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        }
    }

    //Диалог удаления истории
    private fun removeHistoryDialog(context: Context, position: Int, id: Int) {
        //Инициализируем AlertDialog, указываем ему заголовок и описание
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.historyRemoveHistoryTitle))
        builder.setMessage(context.getString(R.string.historyRemoveHistoryDescription))
        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive)) { dialog: DialogInterface?, which: Int ->
            removeHistory(context, position, id)
            Log.d(javaClass.simpleName, context.getString(R.string.historyRemoveHistoryMessage))
        }
        builder.setNeutralButton(context.getText(R.string.buttonNeutral)) { dialog: DialogInterface?, which: Int -> }
        //Показываем AlertDialog
        builder.show()
    }

    //Функция удаления истории из списка
    private fun removeHistory(context: Context, position: Int, id: Int) {
        //calculationsDAO.deleteById(id);
        val uri = ContentUris.withAppendedId(MyContentProvider.Companion.URI_CALCULATIONS, id.toLong())
        //context.getContentResolver().delete()
        context.contentResolver.delete(uri, id.toString(), null)
        histories.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, histories.size)
    }

    // Диалог апдейта истории
    private fun updateHistoryDialog(context: Context, position: Int) {
        // Инициализируем AlertDialog, указываем ему заголовок и описание
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getText(R.string.historyUpdateHistoryTitle))
        builder.setMessage(context.getText(R.string.historyUpdateHistoryDescription))

        // Главный layout
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
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
        var bundle: Bundle = context.contentResolver.call(
                MyContentProvider.Companion.URI_CALCULATIONS, "getFiguresNames", null, null)!!
        val spinnerItems = bundle.getStringArray("spinnerItems")
        val spinnerArray = ArrayList(Arrays.asList(*spinnerItems))
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
        layout.addView(typeLayout)
        // Layout периметра и площади
        val areaAndPerimeter = LinearLayout(context)
        val area = EditText(context)
        val perimeter = EditText(context)
        // Параметры areaAndPerimeter
        val areaAndPerimeterParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val areaParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        areaParams.weight = 1f
        val perimeterParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        perimeterParams.weight = 1f
        // Присваиваем area и parameter значения
        area.setText(histories[position].area.toString())
        perimeter.setText(histories[position].perimeter.toString())
        // Присваиваем параметры areaAndParameter и его элементам
        areaAndPerimeter.layoutParams = areaAndPerimeterParams
        area.layoutParams = areaParams
        perimeter.layoutParams = perimeterParams
        // Добавляем элементы в areaAndPerimeter
        areaAndPerimeter.addView(area)
        areaAndPerimeter.addView(perimeter)
        layout.addView(areaAndPerimeter)

        //Добавляем layout в alertdialog
        builder.setView(layout)
        // builder.setView(areaAndPerimeter);
        bundle = context.contentResolver.call(
                MyContentProvider.Companion.URI_CALCULATIONS, "getFigureId", spinnerText, null)!!
        val figureId = bundle.getInt("figureId")
        val precisedArea = java.lang.Double.valueOf(area.text.toString())
        val precisedPerimeter = java.lang.Double.valueOf(perimeter.text.toString())
        val history = History(histories[position].id, histories[position].dataId,
                spinnerText, precisedArea, precisedPerimeter)

        //Добавляем кнопки и указываем слушатели для них
        builder.setPositiveButton(context.getText(R.string.buttonPositive)) { dialog: DialogInterface?, which: Int ->
            updateHistory(context, position, history, figureId)
            showLog(context, R.string.historyUpdateHistoryMessage)
        }
        builder.setNeutralButton(context.getText(R.string.buttonNeutral)) { dialog: DialogInterface?, which: Int -> }
        //Наконец, показываем AlertDialog
        builder.show()
    }

    // Update
    private fun updateHistory(context: Context, position: Int, history: History, figureId: Int) {
        val id = history.id
        val dataId = history.dataId
        // int new_figureId = figureId;
        val area = history.area
        val perimeter = history.perimeter
        val calculations_values = ContentValues()
        calculations_values.put("id", id)
        calculations_values.put("dataId", dataId)
        calculations_values.put("figureId", figureId)
        calculations_values.put("area", area)
        calculations_values.put("perimeter", perimeter)
        val rowId = context.contentResolver
                .update(MyContentProvider.Companion.URI_CALCULATIONS, calculations_values,
                        null, null)
        Log.d(javaClass.simpleName, rowId.toString())

        //Calculations calculations = new Calculations();
        // calculations.id = history.getId();
        // calculations.dataId =
        // calculations.figureId =
        //  calculations.area =
        //  calculations.perimeter =

        //calculationsDAO.update(calculations);
        histories[position] = history
        notifyItemChanged(position)
    }

    private fun showLog(context: Context, messageId: Int) {
        //Подбираем текст из ресурсов по его id
        val message = context.getString(messageId)
        //Отображение сообщения в логах
        Log.d(javaClass.simpleName, message)
    }
}