package xyz.hvdw.altitude

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class GpsProvider(
    private val context: Context,
    private val callback: (Location) -> Unit
) : LocationCallback(), android.location.LocationListener {

    private val fused = LocationServices.getFusedLocationProviderClient(context)
    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L
    ).build()

    private var lastGpsFix = 0L

    fun start() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val provider = findProvider()
        if (provider != null) {
            lm.requestLocationUpdates(provider, 500, 0f, this)
        }

        fused.requestLocationUpdates(request, this, Looper.getMainLooper())
    }

    fun stop() {
        lm.removeUpdates(this)
        fused.removeLocationUpdates(this)
    }

    override fun onLocationChanged(loc: Location) {
        lastGpsFix = System.currentTimeMillis()
        callback(loc)
    }

    override fun onLocationResult(result: LocationResult) {
        val age = System.currentTimeMillis() - lastGpsFix
        if (age > 5000) {
            result.lastLocation?.let { callback(it) }
        }
    }

    private fun findProvider(): String? {
        val p = lm.allProviders
        return when {
            p.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            p.contains("gps0") -> "gps0"
            p.contains("bd_gps") -> "bd_gps"
            p.contains("nmea") -> "nmea"
            p.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }
}
