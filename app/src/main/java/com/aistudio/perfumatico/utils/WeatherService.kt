package com.aistudio.perfumatico.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val temperature: Float,
    val humidity: Int,
    val isDay: Boolean,
    val city: String
)

object WeatherService {
    suspend fun getLocalWeather(lat: Double, lon: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            // Using Open-Meteo API which doesn't require keys
            val urlStr = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,is_day&timezone=auto"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                
                return@withContext WeatherData(
                    temperature = current.getDouble("temperature_2m").toFloat(),
                    humidity = current.getInt("relative_humidity_2m"),
                    isDay = current.getInt("is_day") == 1,
                    city = "Local Atual" // Real city would require reverse geocoding, we can keep it simple
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
