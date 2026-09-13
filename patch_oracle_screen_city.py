import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/OracleScreen.kt", "r") as f:
    content = f.read()

# Replace the title bar of the weather card
old_weather_title = """
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Cyan500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SEU CLIMA AGORA", color = Cyan500, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
"""

new_weather_title = """
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Cyan500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (weatherData != null) weatherData!!.city.uppercase() else "SEU CLIMA AGORA", color = Cyan500, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
"""
content = content.replace(old_weather_title, new_weather_title)


# Add more options to microclimate
old_climates = """
                val climates = listOf(
                    OracleEngine.Microclimate.OFFICE_AC to "🏢 Escritório (Ar Condicionado)",
                    OracleEngine.Microclimate.MALL_MILD to "🛍️ Shopping (Clima Ameno)",
                    OracleEngine.Microclimate.OUTDOOR_HOT to "☀️ Ar Livre (Calor Natural)",
                    OracleEngine.Microclimate.DATE_NIGHT to "🍷 Encontro (Noite)"
                )
"""

new_climates = """
                val climates = listOf(
                    OracleEngine.Microclimate.OFFICE_AC to "🏢 Escritório (Ar Condicionado Frio)",
                    OracleEngine.Microclimate.MALL_MILD to "🛍️ Shopping / Cinema (Clima Ameno)",
                    OracleEngine.Microclimate.OUTDOOR_HOT to "☀️ Ar Livre / Rua (Temperatura Natural)",
                    OracleEngine.Microclimate.DATE_NIGHT to "🍷 Encontro / Jantar (Noite)",
                    OracleEngine.Microclimate.CLUB_PARTY to "🪩 Balada / Pub (Ambiente Quente e Fechado)",
                    OracleEngine.Microclimate.GYM to "🏋️ Academia / Esporte",
                    OracleEngine.Microclimate.UNKNOWN to "🤷 Não sei / Versátil"
                )
"""
content = content.replace(old_climates, new_climates)

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/OracleScreen.kt", "w") as f:
    f.write(content)
