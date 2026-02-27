package xyz.hvdw.altitude

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import xyz.hvdw.altitude.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // --------------------------------------------------------------------
    // BROADCAST RECEIVER FOR GPS + ALTITUDE UPDATES
    // --------------------------------------------------------------------
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = intent?.getDoubleExtra("lon", 0.0) ?: 0.0
            val acc = intent?.getFloatExtra("accuracy", 0f) ?: 0f

            val hasAltitude = intent?.hasExtra("altitude") == true
            val alt = if (hasAltitude) intent!!.getDoubleExtra("altitude", 0.0) else null

            binding.txtInfo.text =
                if (hasAltitude) {
                    "Lat: %.5f; Lon: %.5f; accuracy: %.1f m; altitude: %.1f m"
                        .format(lat, lon, acc, alt)
                } else {
                    "Lat: %.5f; Lon: %.5f; accuracy: %.1f m; altitude: (pending)"
                        .format(lat, lon, acc)
                }
        }
    }


    // --------------------------------------------------------------------
    // ACTIVITY LIFECYCLE
    // --------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register broadcast receiver
        registerReceiver(dataReceiver, IntentFilter("xyz.hvdw.altitude.UPDATE"))

        // Start service button
        binding.btnStartService.setOnClickListener {
            requestAllPermissions()
        }

        // Stop service button
        binding.btnStopService.setOnClickListener {
            stopService(Intent(this, AltitudeService::class.java))
        }
    }

    override fun onDestroy() {
        unregisterReceiver(dataReceiver)
        super.onDestroy()
    }

    // --------------------------------------------------------------------
    // PERMISSIONS
    // --------------------------------------------------------------------
    private fun requestAllPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        } else {
            startService(Intent(this, AltitudeService::class.java))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == 100) {
            startService(Intent(this, AltitudeService::class.java))
        }
    }
}
