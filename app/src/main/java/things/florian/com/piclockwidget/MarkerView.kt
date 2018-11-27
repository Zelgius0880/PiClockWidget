/*
package things.florian.com.piclockwidget

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*


*/
/**
 * Created by Florian on 03-08-17.
 *//*

class MarkerView(context: Context, layout: Int, val selected: String = "1day") : MarkerView(context, layout) {

    val date: TextView = findViewById(R.id.date)
    val value: TextView = findViewById(R.id.value)

    private var mOffset: MPPointF? = null

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val data = e.data
        val dateFormat= when (selected) {
            "1day" -> SimpleDateFormat("H'h'mm", Locale.getDefault())
            "1week" -> SimpleDateFormat("EE H'h'mm", Locale.getDefault())
            "1month" -> SimpleDateFormat("dd/MM/yyy HH:mm", Locale.getDefault())
            else -> error("Unknown date format")
        }

        when (data) {
            is PressureData -> {
                value.text = String.format(Locale.getDefault(), "%.2f", data.pressure)
                date.text = dateFormat.format(data.date)
            }
            is TemperatureData -> {
                value.text = String.format(Locale.getDefault(), "%.2f", data.temperature)
                date.text = dateFormat.format(data.date)
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {

        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }

        return mOffset as MPPointF
    }
}

class PressureData(val pressure: Double, val date : Date)
class TemperatureData(val temperature: Double, val date : Date)*/
