package xyz.hvdw.altitude

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class AltitudeService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private lateinit var locationManager: LocationManager
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastLocation: Location? = null
    private var lastGpsFixTime = 0L
    private var lastAccuracy = -1f
    private var lastAltitude: Double? = null

    override fun onCreate() {
        super.onCreate()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).build()

        startHybridGps()
        scope.launch { mainLoop() }
    }

    override fun onDestroy() {
        stopHybridGps()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --------------------------------------------------------------------
    // HYBRID GPS START
    // --------------------------------------------------------------------
    private fun startHybridGps() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val provider = findGpsProvider()

        if (provider != null) {
            locationManager.requestLocationUpdates(
                provider,
                200,
                0f,
                gpsListener
            )
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            fusedCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopHybridGps() {
        try { locationManager.removeUpdates(gpsListener) } catch (_: Exception) {}
        try { fusedClient.removeLocationUpdates(fusedCallback) } catch (_: Exception) {}
    }

    // --------------------------------------------------------------------
    // PRIMARY GPS LISTENER
    // --------------------------------------------------------------------
    private val gpsListener = android.location.LocationListener { loc ->
        updateLocation(loc)
    }

    // --------------------------------------------------------------------
    // SECONDARY FUSED CALLBACK
    // --------------------------------------------------------------------
    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val gpsAge = System.currentTimeMillis() - lastGpsFixTime
            if (gpsAge > 5000) {
                for (loc in result.locations) updateLocation(loc)
            }
        }
    }

    // --------------------------------------------------------------------
    // LOCATION UPDATE
    // --------------------------------------------------------------------
    private fun updateLocation(loc: Location) {
        lastLocation = loc
        lastGpsFixTime = System.currentTimeMillis()
        lastAccuracy = loc.accuracy

        sendBroadcastUpdate(loc, null)
    }

    private fun hasGpsFix(): Boolean {
        val age = System.currentTimeMillis() - lastGpsFixTime
        return age < 3000 && lastAccuracy in 0f..25f
    }

    // --------------------------------------------------------------------
    // WATCHDOG
    // --------------------------------------------------------------------
    private fun restartGpsIfNeeded() {
        val age = System.currentTimeMillis() - lastGpsFixTime
        if (age > 8000) {
            mainHandler.post {
                try { locationManager.removeUpdates(gpsListener) } catch (_: Exception) {}
                val provider = findGpsProvider()
                if (provider != null) {
                    locationManager.requestLocationUpdates(
                        provider,
                        200,
                        0f,
                        gpsListener
                    )
                }
            }
        }
    }

    // --------------------------------------------------------------------
    // MAIN LOOP
    // --------------------------------------------------------------------
    private suspend fun mainLoop() {
        while (true) {
            restartGpsIfNeeded()

            val loc = lastLocation
            if (loc != null) {
                scope.launch(Dispatchers.IO) {
                    //val alt = fetchAltitude(loc.latitude, loc.longitude)
                    val alt = getAltitude(loc.latitude, loc.longitude)
                    if (alt != null) {
                        lastAltitude = alt
                    }
                    sendBroadcastUpdate(loc, lastAltitude)
                }
            }

            delay(2000) // Every 2 seconds
        }
    }

    // --------------------------------------------------------------------
    // BROADCAST TO MAINACTIVITY
    // --------------------------------------------------------------------
    private fun sendBroadcastUpdate(loc: Location, altitude: Double?) {
        val intent = Intent("xyz.hvdw.altitude.UPDATE").apply {
            putExtra("lat", loc.latitude)
            putExtra("lon", loc.longitude)
            putExtra("accuracy", loc.accuracy)
            altitude?.let { putExtra("altitude", it) }
        }
        sendBroadcast(intent)
    }


    // --------------------------------------------------------------------
    // PROVIDER DETECTION
    // --------------------------------------------------------------------
    private fun findGpsProvider(): String? {
        val providers = locationManager.allProviders
        return when {
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            providers.contains("gps0") -> "gps0"
            providers.contains("bd_gps") -> "bd_gps"
            providers.contains("nmea") -> "nmea"
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }
}
