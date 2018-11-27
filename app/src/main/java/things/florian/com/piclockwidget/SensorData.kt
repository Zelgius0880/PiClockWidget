package things.florian.com.piclockwidget



/**
 * Created by Florian on 28-07-17.
 */
data class SensorData( override var key: String,
        var temp: Double,
        var elevation: Double,
        var pressure: Double,
        var stamp: Long
): KeyedObject {

    constructor() : this("",0.0, 0.0, 0.0, 0)

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is SensorData -> other.stamp == stamp
            is SensorData? -> other != null && other.stamp == stamp
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = temp.hashCode()
        result = 31 * result + elevation.hashCode()
        result = 31 * result + pressure.hashCode()
        result = 31 * result + stamp.hashCode()
        return result
    }
}
