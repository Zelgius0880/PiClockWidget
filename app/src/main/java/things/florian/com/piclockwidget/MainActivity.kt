package things.florian.com.piclockwidget

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : LifecycleActivity() {

    //val TAG = "MainActivity"
    val day = mutableListOf<TemperatureData>()
    val week = mutableListOf<TemperatureData>()
    val month = mutableListOf<TemperatureData>()

    var pressureSet = BarDataSet(mutableListOf<BarEntry>(), "")
    var convertedTempSet = LineDataSet(mutableListOf<Entry>(), "")
    var convertedTemp2Set = LineDataSet(mutableListOf<Entry>(), "")
    var temperatureSet = LineDataSet(mutableListOf<Entry>(), "")
    var cpuTempSet = LineDataSet(mutableListOf<Entry>(), "")
    var data = CombinedData()
    var selected = "1day"
    var marker: MyMarkerView? = null

    private val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 9001

    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    // [END declare_auth]

    private var mGoogleApiClient: GoogleApiClient? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        pressureSet = BarDataSet(mutableListOf<BarEntry>(), getString(R.string.pressure))
        convertedTempSet = LineDataSet(mutableListOf<Entry>(), getString(R.string.converted_temp))
        convertedTemp2Set = LineDataSet(mutableListOf<Entry>(), getString(R.string.converted_temp))
        temperatureSet = LineDataSet(mutableListOf<Entry>(), getString(R.string.temperature))
        cpuTempSet = LineDataSet(mutableListOf<Entry>(), getString(R.string.cpu_temperature))

        if (chart.data != null) chart.clearValues()
        when (item.itemId) {
            R.id.navigation_1day -> {
                if (selected != "1day") {
                    selected = "1day"
                    marker?.selected = selected
                    viewModel?.select?.value = selected
                    for (data: TemperatureData in day) {
                        addDataToDataSet(data)
                    }
                    setupChart()
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_1week -> {
                if (selected != "1week") {
                    selected = "1week"
                    marker?.selected = selected
                    viewModel?.select?.value = selected
                    for (data: TemperatureData in week) { //Asynchronous?
                        addDataToDataSet(data)
                    }
                    setupChart()
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_1month -> {
                if (selected != "1month") {
                    selected = "1month"
                    marker?.selected = selected
                    viewModel?.select?.value = selected
                    for (data: TemperatureData in month) {//Asynchronous?
                        addDataToDataSet(data)
                    }
                    setupChart()
                }

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    var viewModel: DataViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)

        marker = MyMarkerView(this, R.layout.marker_view)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        viewModel?.data1Day?.observe(this, Observer { data ->
            if (data != null) {
                while (data.size > day.size) {
                    val d = data[day.size]
                    day.add(d)
                    if (selected == "1day") addDataToDataSet(d)
                }
            }
        })

        viewModel?.data1Week?.observe(this, Observer { data ->
            if (data != null) {
                while (data.size > week.size) {
                    val d = data[week.size]
                    week.add(d)
                    if (selected == "1week") addDataToDataSet(d)
                }
            }
        })

        viewModel?.data1Month?.observe(this, Observer { data ->
            if (data != null) {
                while (data.size > month.size) {
                    val d = data[month.size]
                    month.add(d)
                    if (selected == "1month") addDataToDataSet(d)
                }
            }
        })

        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        // [END config_signin]

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        {
                            connectionResult ->
                            run {
                                Log.d(TAG, "onConnectionFailed:" + connectionResult)
                                Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
                            }
                        })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        radioGroup.setOnCheckedChangeListener({ _, id ->
            getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    .putString("SELECTED",
                            when (id) {
                                R.id.radio1Day -> "1day"
                                R.id.radio1Week -> "1Week"
                                R.id.radio1Month -> "1Month"
                                else -> null
                            }
                    ).apply()
        })
    }

    private fun setData() {
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
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setBackgroundColor(Color.WHITE)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.isHighlightFullBarEnabled = false
        chart.marker = marker

        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        //rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        //leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.setValueFormatter({ value, _ -> String.format(Locale.getDefault(), "%.2f", value) })

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        //xAxis.axisMinimum = 0f
        xAxis.granularity = when (selected) {
            "1day" -> 1f
            "1week", "1month" -> 24f
            else -> 1f
        }
        xAxis.setValueFormatter({ value, _ ->
            val size = when (selected) {
                "1day" -> day.size
                "1month" -> month.size
                "1week" -> week.size
                else -> 0
            }
            if (value >= 0 && value < size) {
                when (selected) {
                    "1day" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(day[value.toInt()].stamp))
                    "1month" -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(month[value.toInt()].stamp))
                    "1week" -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(week[value.toInt()].stamp))
                    else -> ""
                }
            } else ""
        })

        val drawCircle = false/*when(selected){
            "1day", "1month" -> false
            else -> true
        }*/

        pressureSet.colors = listOf(getColor(R.color.md_light_blue_700))
        pressureSet.valueTextColor = getColor(R.color.md_light_blue_700)
        pressureSet.valueTextSize = 10f
        pressureSet.setDrawIcons(false)
        pressureSet.axisDependency = YAxis.AxisDependency.RIGHT

        cpuTempSet.color = getColor(R.color.md_deep_orange_A700)
        cpuTempSet.lineWidth = 2.5f
        cpuTempSet.setCircleColor(getColor(R.color.md_deep_orange_A700))
        cpuTempSet.setDrawCircles(drawCircle)
        cpuTempSet.circleRadius = 2f
        cpuTempSet.fillColor = getColor(R.color.md_deep_orange_A700)
        cpuTempSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        cpuTempSet.setDrawValues(true)
        cpuTempSet.valueTextSize = 10f
        cpuTempSet.valueTextColor = getColor(R.color.md_deep_orange_A700)
        cpuTempSet.axisDependency = YAxis.AxisDependency.LEFT

        convertedTemp2Set.color = getColor(R.color.md_amber_A700)
        convertedTemp2Set.lineWidth = 2.5f
        convertedTemp2Set.setCircleColor(getColor(R.color.md_amber_A700))
        convertedTemp2Set.circleRadius = 2f
        convertedTemp2Set.fillColor = getColor(R.color.md_amber_A700)
        convertedTemp2Set.setDrawCircles(drawCircle)
        convertedTemp2Set.mode = LineDataSet.Mode.CUBIC_BEZIER
        convertedTemp2Set.setDrawValues(true)
        convertedTemp2Set.valueTextSize = 10f
        convertedTemp2Set.valueTextColor = getColor(R.color.md_amber_A700)
        convertedTemp2Set.axisDependency = YAxis.AxisDependency.LEFT

        temperatureSet.color = getColor(R.color.md_amber_200)
        temperatureSet.lineWidth = 2.5f
        temperatureSet.setCircleColor(getColor(R.color.md_amber_200))
        temperatureSet.circleRadius = 2f
        temperatureSet.fillColor = getColor(R.color.md_amber_200)
        temperatureSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        temperatureSet.setDrawValues(true)
        temperatureSet.setDrawCircles(drawCircle)
        temperatureSet.valueTextSize = 10f
        temperatureSet.valueTextColor = getColor(R.color.md_amber_200)
        temperatureSet.axisDependency = YAxis.AxisDependency.LEFT

        convertedTempSet.color = getColor(R.color.md_orange_300)
        convertedTempSet.lineWidth = 2.5f
        convertedTempSet.setCircleColor(getColor(R.color.md_orange_300))
        convertedTempSet.circleRadius = 2f
        convertedTempSet.fillColor = getColor(R.color.md_orange_300)
        convertedTempSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        convertedTempSet.setDrawCircles(drawCircle)
        convertedTempSet.setDrawValues(true)
        convertedTempSet.valueTextSize = 10f
        convertedTempSet.valueTextColor = getColor(R.color.md_orange_300)
        convertedTempSet.axisDependency = YAxis.AxisDependency.LEFT

        setData()

        chart.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.LINE)
        chart.data = data
    }

    override fun onStart() {
        super.onStart()
        user = mAuth?.currentUser
        if (user == null) signIn()
    }

    override fun onResume() {
        super.onResume()

        if (user != null) {
            pressureSet = BarDataSet(mutableListOf<BarEntry>(), getString(R.string.pressure))
            convertedTempSet = LineDataSet(mutableListOf<Entry>(), getString(R.string.converted_temp))
            convertedTemp2Set = LineDataSet(mutableListOf<Entry>(), getString(R.string.converted_temp))
            temperatureSet = LineDataSet(mutableListOf<Entry>(), getString(R.string.temperature))
            cpuTempSet = LineDataSet(mutableListOf<Entry>(), getString(R.string.cpu_temperature))

            if (chart.data != null) chart.clearValues()

            viewModel?.init(FirebaseDatabase.getInstance())
        }

        if (viewModel?.select?.value != null)
            selected = viewModel?.select?.value as String

        when (selected) {
            "1day" -> navigation.selectedItemId = R.id.navigation_1day
            "1month" -> navigation.selectedItemId = R.id.navigation_1month
            "1week" -> navigation.selectedItemId = R.id.navigation_1week
        }
    }

    fun addDataToDataSet(d: TemperatureData) {
        pressureSet.addEntry(BarEntry(pressureSet.entryCount.toFloat() - 1, d.pressure.toFloat(), null, d))
        temperatureSet.addEntry(Entry(temperatureSet.entryCount.toFloat() - 1, d.measuredTemp.toFloat(), null, d))
        convertedTempSet.addEntry(Entry(convertedTempSet.entryCount.toFloat() - 1, d.convertedTemp.toFloat(), null, d))
        convertedTemp2Set.addEntry(Entry(convertedTemp2Set.entryCount.toFloat() - 1, d.convertedTemp2.toFloat(), null, d))
        cpuTempSet.addEntry(Entry(cpuTempSet.entryCount.toFloat() - 1, d.cpuTemp.toFloat(), null, d))
        if (data.barData == null || data.lineData == null) setupChart()
        data.notifyDataChanged()
        chart.notifyDataSetChanged()
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Google Sign In was successful, authenticate with Firebase
                val account = result.signInAccount
                if (account != null) firebaseAuthWithGoogle(account)
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                Toast.makeText(this, "Google Sign In failed, update UI appropriately. Code:" + result.status, Toast.LENGTH_LONG).show()
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
        // [START_EXCLUDE silent]
        //showProgressDialog()
        // [END_EXCLUDE]

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        user = mAuth?.currentUser
                        viewModel?.init(FirebaseDatabase.getInstance())
                        //updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(this@MainActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                    // [START_EXCLUDE]
                    //hideProgressDialog()
                    // [END_EXCLUDE]
                }
    }
    // [END auth_with_google]

}
