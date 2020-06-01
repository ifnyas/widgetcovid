package app.ifnyas.widgetcovid

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.ifnyas.widgetcovid.api.ApiClient
import app.ifnyas.widgetcovid.api.ApiService
import app.ifnyas.widgetcovid.utils.AppWidget
import app.ifnyas.widgetcovid.utils.AppWorker
import app.ifnyas.widgetcovid.utils.SessionManager
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + handler

    private val handler =
        CoroutineExceptionHandler { _, t ->
            addLog("${this.javaClass.name} ${t.stackTrace[0].lineNumber}: $t", 1)
        }

    private lateinit var job: Job
    private lateinit var sm: SessionManager

    lateinit var lm: LocationManager
    lateinit var loc: Location

    data class Place(
        val title: String,
        val category: String,
        val distance: Double
    )

    private var placeList = mutableListOf<Place>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init val
        job = Job()
        sm = SessionManager(this)

        // check permission
        checkPermit()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun checkPermit() = runWithPermissions(
        Manifest.permission.ACCESS_FINE_LOCATION,
        options = QuickPermissionsOptions(permissionsDeniedMethod = { finish() })
    ) {
        initFun()
    }

    private fun initFun() {
        loc_btn.setOnClickListener {
            progressToggle(true)
            reqLoc()
        }
    }

    private fun reqLoc() {

        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        addLog("requesting loc...", 0)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermit()
        }

        lm.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L, 1000f, locationListener
        )
    }

    private fun setLoc() {
        // remove location listener
        //lm.removeUpdates(locationListener)

        // set text
        val locText = "${loc.latitude},${loc.longitude}"
        loc_text.text = locText

        // next fun
        addLog("Getting area...", 0)
        launch {
            val rawanCode = withContext(Dispatchers.IO) { getRawan() }
            if (rawanCode == "200") {
                for (i in placeList.indices) {
                    addLog("${placeList[i]}", 0)
                }
                setNear()
            } else {
                addLog("Finished status $rawanCode", 1)
            }
        }
    }

    private fun getRawan(): String {
        // init API Service
        val msg: String
        val apiService = ApiClient.pandemyClient.create(ApiService::class.java)

        // get data
        try {
            val req = apiService.getRawan("${loc_text.text}").execute()
            msg = "${req.code()}"

            if (msg == "200") {
                val res = JSONArray(req.body()!!.string())
                if (res.length() > 0) {
                    for (i in 0 until res.length()) {
                        val record = res.getJSONObject(i)
                        val rTitle = record.getString("title")
                        val rCategory = record.getString("kategori")
                        val rDistance = record.getDouble("jarak")
                        val place = Place(rTitle, rCategory, rDistance)
                        placeList.add(place)
                    }
                }
            }
        } catch (e: Exception) {
            return "$e"
        }

        return msg
    }

    private fun setNear() {
        if (placeList.isNotEmpty()) {
            val nearest = placeList.minBy { it.distance }!!
            val resultText = "${nearest.title},${nearest.category}"
            result_text.text = resultText
            sm.postPlace(resultText)
        } else {
            result_text.text = "NONE"
            sm.postPlace("Aman")
        }
        addLog("Finished.", 1)
    }

    private fun addLog(msg: String, isFinished: Int) {
        if (isFinished == 1) {
            progressToggle(false)
            //updateWidget()
            initWorker()

            // debug
            loc_btn.setOnClickListener { recreate() }
        }
        val oldText = log_text.text
        val newText = "$oldText\n$msg"
        log_text.text = newText
    }

    private fun updateWidget() {
        val intent = Intent(this, AppWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(
                applicationContext, AppWidget::class.java
            )
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun initWorker() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateWorkRequest =
            PeriodicWorkRequestBuilder<AppWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueue(updateWorkRequest)
    }

    private fun progressToggle(isLoading: Boolean) = if (isLoading) {
        progress_bar.visibility = View.VISIBLE
        loc_btn.isClickable = !isLoading
    } else {
        progress_bar.visibility = View.INVISIBLE
        loc_btn.isClickable = isLoading
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(l: Location) {
            loc = l; setLoc()
        }

        override fun onStatusChanged(p: String, s: Int, e: Bundle) {}
        override fun onProviderEnabled(p: String) {}
        override fun onProviderDisabled(p: String) {}
    }
}