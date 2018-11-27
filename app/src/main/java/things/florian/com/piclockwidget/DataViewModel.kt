package things.florian.com.piclockwidget

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Florian on 01-08-17.
 */
class DataViewModel(val app: Application) : AndroidViewModel(app) {
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    private val myRef: DatabaseReference by lazy {
        database.getReference("TempData")
    }
    /*private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }*/
    //var user: FirebaseUser? = null

    val data =  FirebaseLiveData(SensorData::class.java)

    val current = FirebaseLiveData(SensorData::class.java)

    init {
        current.query = myRef.child("current")
    }


    /**
     * @param date the max date to get the data
     * @return the data between date and date - 1day
     */
    fun getOneDay(date: Date){
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DATE, -1)
        }

        Log.e(DataViewModel::class.java.name, SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(calendar.time))

        data.query = myRef.child("data").orderByChild("stamp")
                .startAt(calendar.timeInMillis.toDouble())
                .endAt(date.time.toDouble())
    }

    /**
     * @param date the max date to get the data
     * @return the data between date and date - 1week
     */
    fun getOneWeek(date: Date){
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, -1)
        }

        data.query = myRef.child("data").orderByChild("stamp")
                .startAt(calendar.timeInMillis.toDouble())
                .endAt(date.time.toDouble())
    }

    /**
     * @param date the max date to get the data
     * @return the data between date and date - 1month
     */
    fun getOneMonth(date: Date){
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, -31)
        }

        data.query = myRef.child("data").orderByChild("stamp")
                .startAt(calendar.timeInMillis.toDouble())
                .endAt(date.time.toDouble())
    }


    /**
     * @param date the max date to get the data
     * @return the data between date and date - 1year
     */
    fun getOneYear(date: Date){
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, -365)
        }

        data.query = myRef.child("data").orderByChild("stamp")
                .startAt(calendar.timeInMillis.toDouble())
                .endAt(date.time.toDouble())
    }
}