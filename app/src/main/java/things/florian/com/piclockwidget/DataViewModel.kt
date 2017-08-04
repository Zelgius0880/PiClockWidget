package things.florian.com.piclockwidget

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Florian on 01-08-17.
 */
class DataViewModel : ViewModel() {
    val data1Day = MutableLiveData<MutableList<TemperatureData>>()
    val data1Week = MutableLiveData<MutableList<TemperatureData>>()
    val data1Month = MutableLiveData<MutableList<TemperatureData>>()

    val select = MutableLiveData<String>()

    init {
        select.value = "1day"
    }

    var query: Query? = null

    fun init(database: FirebaseDatabase) {
        val myRef = database.getReference("TempData")

        data1Month.value = mutableListOf<TemperatureData>()
        data1Week.value = mutableListOf<TemperatureData>()
        data1Day.value = mutableListOf<TemperatureData>()

        query?.removeEventListener(listener)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        query = myRef.child("data").startAt(calendar.timeInMillis.toDouble()).orderByChild("stamp")
        query?.addChildEventListener(listener)
    }

    val listener = object : ChildEventListener {
        val week: Date
        val day: Date

        init {
            var calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_MONTH, -1)
            week = calendar.time

            calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1)
            day = calendar.time
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val data = dataSnapshot.getValue(TemperatureData::class.java)
            if(data != null ) {
                if (Date(data.stamp).after(day) && !data1Day.value!!.contains(data)) {
                    data1Day.value?.add(data)
                    data1Day.value = data1Day.value
                }

                if (Date(data.stamp).after(week) && !data1Week.value!!.contains(data)) {
                    data1Week.value?.add(data)
                    data1Week.value = data1Week.value
                }

                data1Month.value?.add(data)
                data1Month.value = data1Month.value
            }

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    val weekListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val data = dataSnapshot.getValue(TemperatureData::class.java)
            if (data != null && !data1Week.value!!.contains(data))
                data1Week.value?.add(data)

            data1Week.value = data1Week.value
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    val monthListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val data = dataSnapshot.getValue(TemperatureData::class.java)
            if (data != null && !data1Month.value!!.contains(data))
                data1Month.value?.add(data)

            data1Month.value = data1Month.value
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }
}