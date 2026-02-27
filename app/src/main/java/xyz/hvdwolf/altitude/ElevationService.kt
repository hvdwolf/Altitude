package xyz.hvdw.altitude

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


fun getAltitude(lat: Double, lon: Double): Double? {
    val client = OkHttpClient()

    val url = "https://api.open-elevation.com/api/v1/lookup?locations=$lat,$lon"
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return null

        val body = response.body?.string() ?: return null
        val json = JSONObject(body)

        val results = json.getJSONArray("results")
        if (results.length() == 0) return null

        return results.getJSONObject(0).getDouble("elevation")
    }
}
