package things.florian.com.piclockwidget

import com.google.firebase.database.IgnoreExtraProperties


/**
 * Created by Florian on 28-07-17.
 */
@IgnoreExtraProperties
data class TemperatureData (
    var cpuTemp: Double,
    var measuredTemp: Double,
    var convertedTemp: Double,
    var convertedTemp2: Double,
    var pressure: Double,
    var stamp: Long
) {

    constructor() : this(0.0,0.0,0.0,0.0,0.0,0)

    override fun equals(other: Any?): Boolean {
        return when (other){
            is TemperatureData -> other.stamp == stamp
            is TemperatureData? -> other != null && other.stamp == stamp
            else -> false
        }
    }
}
