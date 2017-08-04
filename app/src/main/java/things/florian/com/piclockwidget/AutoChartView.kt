package things.florian.com.piclockwidget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Florian on 04-08-17.
 */
class AutoChartView : FrameLayout{
    constructor(context: Context) : super(context){
        init(context)
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet){
        init(context)

    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr){
        init(context)

    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes){
        init(context)
    }

    private fun init(context: Context) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("TempData")
        val calendar = Calendar.getInstance()
        val selected = context.getSharedPreferences("PREFS", Activity.MODE_PRIVATE).getString("SELECTED", "1day")
        calendar.add(when (selected) {
            "1day" -> Calendar.DATE
            "1week" -> Calendar.WEEK_OF_YEAR
            "1month" -> Calendar.MONTH
            else -> Calendar.DATE
        }, -1)
        val query = myRef.child("data").startAt(calendar.timeInMillis.toDouble()).orderByChild("stamp")
        query?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError?) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = mutableListOf<TemperatureData>()
                for (postSnapshot in dataSnapshot.children) {
                    val data = dataSnapshot.getValue(TemperatureData::class.java)
                    if(data != null) list.add(data)
                }

                setupChart(context, list, selected)
                query.removeEventListener(this)
            }

        })
    }

    private fun setupChart(context: Context, list: List<TemperatureData>, selected :String) {
        val pressureSet = BarDataSet(mutableListOf<BarEntry>(), "")
        val convertedTempSet = LineDataSet(mutableListOf<Entry>(), "")
        val convertedTemp2Set = LineDataSet(mutableListOf<Entry>(), "")
        val temperatureSet = LineDataSet(mutableListOf<Entry>(), "")
        val cpuTempSet = LineDataSet(mutableListOf<Entry>(), "")
        val data = CombinedData()

        for(d: TemperatureData in list){
            pressureSet.addEntry(BarEntry(pressureSet.entryCount.toFloat() - 1, d.pressure.toFloat(), null, d))
            temperatureSet.addEntry(Entry(temperatureSet.entryCount.toFloat() - 1, d.measuredTemp.toFloat(), null, d))
            convertedTempSet.addEntry(Entry(convertedTempSet.entryCount.toFloat() - 1, d.convertedTemp.toFloat(), null, d))
            convertedTemp2Set.addEntry(Entry(convertedTemp2Set.entryCount.toFloat() - 1, d.convertedTemp2.toFloat(), null, d))
            cpuTempSet.addEntry(Entry(cpuTempSet.entryCount.toFloat() - 1, d.cpuTemp.toFloat(), null, d))
        }

        val chart = CombinedChart(context)
        chart.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        chart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        addView(chart)

        chart.description.isEnabled = false
        chart.setBackgroundColor(Color.WHITE)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.isHighlightFullBarEnabled = false

        chart.legend.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        //rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        //leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.setValueFormatter({ value, _ -> String.format(Locale.getDefault(), "%.2f", value) })

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.axisMinimum = 0f
        xAxis.granularity = when (selected) {
            "1day" -> 1f
            "1week", "1month" -> 24f
            else -> 1f
        }
        xAxis.setValueFormatter({ value, _ ->

            if (value >= 0 && value < list.size) {
                when (selected) {
                    "1day" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(list[value.toInt()].stamp))
                    "1month" -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(list[value.toInt()].stamp))
                    "1week" -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(list[value.toInt()].stamp))
                    else -> ""
                }
            } else ""
        })

        val drawCircle = false

        pressureSet.colors = listOf(ContextCompat.getColor(context, R.color.md_light_blue_700))
        pressureSet.valueTextColor = ContextCompat.getColor(context, R.color.md_light_blue_700)
        pressureSet.valueTextSize = 10f
        pressureSet.setDrawIcons(false)
        pressureSet.axisDependency = YAxis.AxisDependency.RIGHT

        cpuTempSet.color = ContextCompat.getColor(context, R.color.md_deep_orange_A700)
        cpuTempSet.lineWidth = 2.5f
        cpuTempSet.setCircleColor(ContextCompat.getColor(context, R.color.md_deep_orange_A700))
        cpuTempSet.setDrawCircles(drawCircle)
        cpuTempSet.circleRadius = 2f
        cpuTempSet.fillColor = ContextCompat.getColor(context, R.color.md_deep_orange_A700)
        cpuTempSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        cpuTempSet.setDrawValues(true)
        cpuTempSet.valueTextSize = 10f
        cpuTempSet.valueTextColor = ContextCompat.getColor(context, R.color.md_deep_orange_A700)
        cpuTempSet.axisDependency = YAxis.AxisDependency.LEFT

        convertedTemp2Set.color = ContextCompat.getColor(context, R.color.md_amber_A700)
        convertedTemp2Set.lineWidth = 2.5f
        convertedTemp2Set.setCircleColor(ContextCompat.getColor(context, R.color.md_amber_A700))
        convertedTemp2Set.circleRadius = 2f
        convertedTemp2Set.fillColor = ContextCompat.getColor(context, R.color.md_amber_A700)
        convertedTemp2Set.setDrawCircles(drawCircle)
        convertedTemp2Set.mode = LineDataSet.Mode.CUBIC_BEZIER
        convertedTemp2Set.setDrawValues(true)
        convertedTemp2Set.valueTextSize = 10f
        convertedTemp2Set.valueTextColor = ContextCompat.getColor(context, R.color.md_amber_A700)
        convertedTemp2Set.axisDependency = YAxis.AxisDependency.LEFT

        temperatureSet.color = ContextCompat.getColor(context, R.color.md_amber_200)
        temperatureSet.lineWidth = 2.5f
        temperatureSet.setCircleColor(ContextCompat.getColor(context, R.color.md_amber_200))
        temperatureSet.circleRadius = 2f
        temperatureSet.fillColor = ContextCompat.getColor(context, R.color.md_amber_200)
        temperatureSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        temperatureSet.setDrawValues(true)
        temperatureSet.setDrawCircles(drawCircle)
        temperatureSet.valueTextSize = 10f
        temperatureSet.valueTextColor = ContextCompat.getColor(context, R.color.md_amber_200)
        temperatureSet.axisDependency = YAxis.AxisDependency.LEFT

        convertedTempSet.color = ContextCompat.getColor(context, R.color.md_orange_300)
        convertedTempSet.lineWidth = 2.5f
        convertedTempSet.setCircleColor(ContextCompat.getColor(context, R.color.md_orange_300))
        convertedTempSet.circleRadius = 2f
        convertedTempSet.fillColor = ContextCompat.getColor(context, R.color.md_orange_300)
        convertedTempSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        convertedTempSet.setDrawCircles(drawCircle)
        convertedTempSet.setDrawValues(true)
        convertedTempSet.valueTextSize = 10f
        convertedTempSet.valueTextColor = ContextCompat.getColor(context, R.color.md_orange_300)
        convertedTempSet.axisDependency = YAxis.AxisDependency.LEFT

        val barData = BarData()
        barData.addDataSet(pressureSet)
        barData.barWidth = 0.9f
        barData.setValueTextSize(10f)
        data.setData(barData)

        val lineData = LineData()
        lineData.addDataSet(cpuTempSet)
        lineData.addDataSet(temperatureSet)
        lineData.addDataSet(convertedTempSet)
        lineData.addDataSet(convertedTemp2Set)
        data.setData(lineData)

        chart.drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE)
        chart.data = data
    }

}