package things.florian.com.piclockwidget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.LabelFormatter
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_one_day.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class OneMonthFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private val model by lazy { ViewModelProviders.of(this).get(DataViewModel::class.java) }

    var temperatureSeries: LineGraphSeries<DataPoint>? = null
    var pressureSeries: BarGraphSeries<DataPoint>? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyy", Locale.getDefault())
    private var currentDate = Date()
    private val ctx by lazy { activity!! }
    private val sensorData = mutableListOf<MutableList<SensorData>>()
    private val temperatureEntities = mutableListOf<DataPoint>()
    private val pressureEntities = mutableListOf<DataPoint>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set manual X bounds
        chart.viewport.apply {
            isYAxisBoundsManual = true

            isXAxisBoundsManual = true
            setMinX(0.0)
            setMaxX(372.0)

            // enable scaling and scrolling
            isScalable = true
            setScalableY(true)

            isScrollable = true
            setScrollableY(true)
        }

        chart.gridLabelRenderer.labelFormatter = MyLabelFormatter()

        formatDate(currentDate)

        model.data.observe(this, Observer {
            //Log.d(OneDayFragment::javaClass.name, "${it?.size}")
            if (it != null && it.isNotEmpty()) {
                //fillChart(it)
                val addedAtEnd = !addData(it.last())
                setElevation()

                if (pressureSeries == null) {
                    pressureSeries = BarGraphSeries(pressureEntities.toTypedArray()).apply {
                        //isDrawBackground = true
                        isAnimated = true
                        //isDrawDataPoints = true
                        title = getString(R.string.pressure)
                        spacing = 20
                        color = ContextCompat.getColor(ctx, R.color.md_light_blue_100)
                        setOnDataPointTapListener { series, dataPoint ->
                            showData(dataPoint)
                        }

                        // draw values on top
                        /*isDrawValuesOnTop = true
                        setValuesOnTopColor(Color.RED)*/
                    }

                    chart.addSeries(pressureSeries)
                } else {
                    if (addedAtEnd)
                        pressureSeries?.appendData(pressureEntities.last(), true, 744)
                    else {
                        pressureSeries?.resetData(pressureEntities.toTypedArray())
                    }
                }

                if (temperatureSeries == null) {
                    temperatureSeries = LineGraphSeries(temperatureEntities.toTypedArray()).apply {
                        //isDrawBackground = true
                        setAnimated(true)
                        isDrawDataPoints = true
                        title = getString(R.string.temperature)
                        color = ContextCompat.getColor(ctx, R.color.md_red_300)
                        dataPointsRadius = 2f
                        setOnDataPointTapListener { series, dataPoint ->
                            showData(dataPoint)
                        }
                    }

                    chart.secondScale.addSeries(temperatureSeries)
                } else {
                    if (addedAtEnd)
                        temperatureSeries?.appendData(temperatureEntities.last(), true,744)
                    else {
                        temperatureSeries?.resetData(temperatureEntities.toTypedArray())
                    }
                }

                setMaxMin()

            }
        })

        temperatuePressure.text = String.format("${getString(R.string.temperature_format)} " +
                "- ${getString(R.string.pressure_format)} ", 0.0,0.0)
        model.current.observe(this, Observer {
            //Log.e(OneDayFragment::javaClass.name, it?.toString())
            if(it.isNotEmpty()) {
                elevation.text = String.format(getString(R.string.elevation_format), it[0].elevation)
                temperatuePressure.text = String.format("${getString(R.string.temperature_format)} " +
                        "- ${getString(R.string.pressure_format)} ", it[0].temp, it[0].pressure)
            }
        })

        model.getOneMonth(Date())

        today.setOnClickListener {
            sensorData.clear()
            temperatureEntities.clear()
            pressureEntities.clear()
            temperatureSeries?.resetData(temperatureEntities.toTypedArray())
            pressureSeries?.resetData(pressureEntities.toTypedArray())
            temperatureSeries = null
            pressureSeries = null
            chart.removeAllSeries()

            currentDate = Date()
            formatDate(currentDate)
            model.getOneMonth(currentDate)
        }

        previous.setOnClickListener {
            currentDate = Calendar.getInstance().apply {
                time = currentDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)

                add(Calendar.DATE, -31)
            }.time

            formatDate(currentDate)
            sensorData.clear()
            temperatureEntities.clear()
            pressureEntities.clear()
            temperatureSeries?.resetData(temperatureEntities.toTypedArray())
            pressureSeries?.resetData(pressureEntities.toTypedArray())
            temperatureSeries = null
            pressureSeries = null
            chart.removeAllSeries()

            model.getOneMonth(currentDate)

            val c = Calendar.getInstance().apply {
                time = currentDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)

                add(Calendar.DATE, 31)
            }

            next.isEnabled = !c.time.after(Date())
        }

        next.isEnabled = false
        next.setOnClickListener {
            val c = Calendar.getInstance().apply {
                time = currentDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)

                add(Calendar.DATE, 31)
            }

            if(c.time.before(Date())) {

                currentDate = c.time

                date.text = dateFormat.format(currentDate)
                sensorData.clear()
                temperatureEntities.clear()
                pressureEntities.clear()
                temperatureSeries?.resetData(temperatureEntities.toTypedArray())
                pressureSeries?.resetData(pressureEntities.toTypedArray())
                temperatureSeries = null
                pressureSeries = null
                chart.removeAllSeries()

                model.getOneMonth(currentDate)
            }

            c.apply {
                time = currentDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)

                add(Calendar.DATE, 31)
            }

            next.isEnabled = !c.time.after(Date())
        }
    }

    private fun formatDate(d: Date){
        date.text = String.format("%s - %s", dateFormat.format(Calendar.getInstance().apply {
            time = d
            add(Calendar.DATE, -31)
        }.time), dateFormat.format(d))


    }

    private fun showData(dataPoint: DataPointInterface) {
        if(dataPoint.x < sensorData.size) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            data.visibility = View.VISIBLE
            temperature.text = String.format(getString(R.string.temperature_format), temperatureEntities[dataPoint.x.toInt()].y)
            pressure.text = String.format(getString(R.string.pressure_format), pressureEntities[dataPoint.x.toInt()].y)
            selectedDate.text = Calendar.getInstance().let {
                it.time = currentDate
                it.set(Calendar.MINUTE, 0)

                it.add(Calendar.HOUR_OF_DAY, dataPoint.x.toInt() - 744)
                dateFormat.format(it.time)
            }
        } else {
            data.visibility = View.GONE
        }
    }

    private fun setMaxMin() {
        chart.secondScale.setMaxY((temperatureEntities.maxWith(kotlin.Comparator { a, b ->
            when {
                a.y < b.y -> -1
                a.y > b.y -> 1
                else -> 0
            }
        })?.y ?: 0.0) + 5)

        chart.secondScale.setMinY((temperatureEntities.minWith(kotlin.Comparator { a, b ->
            when {
                a.y < b.y -> -1
                a.y > b.y -> 1
                else -> 0
            }
        })?.y ?: 0.0) - 5)

        chart.viewport.setMaxY((pressureEntities.maxWith(kotlin.Comparator { a, b ->
            when {
                a.y < b.y -> -1
                a.y > b.y -> 1
                else -> 0
            }
        })?.y ?: 0.0) + 200)

        chart.viewport.setMinY((pressureEntities.minWith(kotlin.Comparator { a, b ->
            when {
                a.y < b.y -> -1
                a.y > b.y -> 1
                else -> 0
            }
        })?.y ?: 0.0) - 200)
    }

    private fun setElevation(){
        var e = 0.0
        var  i = 0
        sensorData.forEach{array ->
            array.forEach {
                e+=it.elevation
                ++i
            }
        }
        elevation.text = String.format(getString(R.string.elevation_format), e/i)
    }

    /**
     * @return true if data is not added at the end of the list.
     * It data is not added at the end of the list, the chart need be invalidate and the char entities (Bar and Line)
     * at the position of data will be modified with the average of the pressure and temperature contained in the list
     */
    private fun addData(data: SensorData): Boolean {
        val index = 744 - (TimeUnit.HOURS
                .convert(currentDate.time - data.stamp, TimeUnit.MILLISECONDS)).toInt()

        if (index >= 0 && index >= sensorData.size) { //just in case
            if (index >= sensorData.size) {
                val size = sensorData.size
                for(i in size .. index) {
                    sensorData.add(mutableListOf(data))
                    pressureEntities.add(DataPoint(i + 0.0, data.pressure))
                    temperatureEntities.add(DataPoint(i + 0.0, data.temp))
                }

                return false
            } else {
                //Log.d(OneDayFragment::javaClass.name, "set $index")
                sensorData[index].add(data)

                var pressure = 0.0
                var temperature = 0.0
                sensorData[index].forEach {
                    pressure += it.pressure
                    temperature += it.temp
                }

                pressureEntities[index] = DataPoint(index + 0.0, pressure / sensorData[index].size)
                temperatureEntities[index] = DataPoint(index + 0.0, temperature / sensorData[index].size)
            }
        }

        return true
    }

    inner class MyLabelFormatter : DefaultLabelFormatter() {
        var vp: Viewport? = null
        override fun setViewport(viewport: Viewport?) {
            this.vp = viewport
        }

        override fun formatLabel(value: Double, isValueX: Boolean): String {
            return if (!isValueX) {
                String.format(if (value > 500) "%.0f" else "%.1f", value)
            } else {
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                Calendar.getInstance().let {
                    it.time = currentDate
                    it.set(Calendar.MINUTE, 0)

                    it.add(Calendar.HOUR_OF_DAY, value.toInt() - 744)
                    //if (value.toInt() % 2 == 0)
                    dateFormat.format(it.time)
                    // else ""
                }
            }
        }
    }

    companion object {
/*
        @JvmStatic
        fun newInstance() =
                OneDayFragment().apply {
                    arguments = Bundle().apply {
                    }
                }*/
    }
}
