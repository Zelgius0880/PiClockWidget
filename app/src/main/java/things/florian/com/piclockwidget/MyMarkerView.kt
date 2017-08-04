package things.florian.com.piclockwidget

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Florian on 03-08-17.
 */
class MyMarkerView(context: Context, layout: Int) : MarkerView(context, layout) {

    val date: TextView = findViewById(R.id.date)
    val value: TextView = findViewById(R.id.value)

    var selected = "1day"

    private var mOffset: MPPointF? = null

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val data = e.data as TemperatureData
        date.text = when (selected) {
            "1day" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(data.stamp))
            "1week" -> SimpleDateFormat("EE HH:mm", Locale.getDefault()).format(Date(data.stamp))
            "1month" -> SimpleDateFormat("dd/MM/yyy HH:mm", Locale.getDefault()).format(Date(data.stamp))
            else -> ""
        }
        value.text = String.format(Locale.getDefault(), "%.2f", e.y)
        // this will perform necessary layouting
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