import re

with open("app/src/main/java/com/aistudio/perfumatico/utils/WeatherService.kt", "r") as f:
    content = f.read()

old_logic = """
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
"""

new_logic = """
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                
                // Get city name using Open-Meteo Geocoding reverse (simplification)
                var cityName = "Fortaleza, CE" // Default fallback for demo
                try {
                    val geoUrl = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon")
                    val geoConn = geoUrl.openConnection() as HttpURLConnection
                    geoConn.setRequestProperty("User-Agent", "PerfumaticoApp")
                    if (geoConn.responseCode == 200) {
                        val geoResp = geoConn.inputStream.bufferedReader().use { it.readText() }
                        val geoJson = JSONObject(geoResp)
                        val address = geoJson.optJSONObject("address")
                        if (address != null) {
                            val city = address.optString("city", address.optString("town", address.optString("village", "")))
                            val state = address.optString("state", "")
                            if (city.isNotEmpty()) {
                                cityName = if (state.isNotEmpty()) "$city, $state" else city
                            }
                        }
                    }
                } catch(e: Exception) {
                    // Ignore reverse geocoding error
                }
                
                return@withContext WeatherData(
                    temperature = current.getDouble("temperature_2m").toFloat(),
                    humidity = current.getInt("relative_humidity_2m"),
                    isDay = current.getInt("is_day") == 1,
                    city = cityName
                )
            }
"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/perfumatico/utils/WeatherService.kt", "w") as f:
    f.write(content)
