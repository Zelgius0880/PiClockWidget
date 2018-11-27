package things.florian.com.piclockwidget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    private val navController by lazy {Navigation.findNavController(this, R.id.my_nav_host_fragment)}
    //private lateinit var mGoogleApiClient: GoogleApiClient

    override fun onTabReselected(tab: TabLayout.Tab) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab) {

    }

    override fun onTabSelected(tab: TabLayout.Tab) {

        when(tab.position){
            0 -> navController.navigate(R.id.oneDayFragment)
            1 -> navController.navigate(R.id.oneWeekFragment)
            2 -> navController.navigate(R.id.oneMonthFragment)
            3 -> navController.navigate(R.id.oneYearFragment)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabs.removeOnTabSelectedListener(this)
        tabs.addOnTabSelectedListener(this)
    }



}
