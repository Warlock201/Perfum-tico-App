import re

with open("app/src/main/java/com/aistudio/perfumatico/utils/OracleEngine.kt", "r") as f:
    content = f.read()

old_enum = """
    enum class Microclimate {
        OFFICE_AC,      // Ar condicionado forte
        MALL_MILD,      // Shopping/Clima Ameno
        OUTDOOR_HOT,    // Ar livre/Calor
        DATE_NIGHT      // Encontro à noite
    }
"""

new_enum = """
    enum class Microclimate {
        OFFICE_AC,      // Ar condicionado forte
        MALL_MILD,      // Shopping/Clima Ameno
        OUTDOOR_HOT,    // Ar livre/Calor
        DATE_NIGHT,     // Encontro à noite
        CLUB_PARTY,     // Balada
        GYM,            // Academia
        UNKNOWN         // Versatil
    }
"""
content = content.replace(old_enum, new_enum)

old_when = """
                Microclimate.DATE_NIGHT -> {
                    // Date night.
                    if (tags.contains("ENCONTRO") || tags.contains("NOITE")) score += 6
                    if (!weather.isDay) score += 2
                    if (temp > 27f && isDense) score -= 3 // Hot night, careful with dense
                    if (temp <= 27f && isDense) score += 4 // Cool night, perfect for dense
                }
            }
"""

new_when = """
                Microclimate.DATE_NIGHT -> {
                    if (tags.contains("ENCONTRO") || tags.contains("NOITE")) score += 6
                    if (!weather.isDay) score += 2
                    if (temp > 27f && isDense) score -= 3
                    if (temp <= 27f && isDense) score += 4
                }
                Microclimate.CLUB_PARTY -> {
                    if (tags.contains("BALADA") || tags.contains("NOITE")) score += 8
                    if (perfume.projection >= 8) score += 4 // Need loud fragrances
                    if (isFresh && perfume.projection < 7) score -= 5 // Too weak
                }
                Microclimate.GYM -> {
                    if (tags.contains("ACADEMIA") || tags.contains("DIA A DIA")) score += 6
                    if (isFresh) score += 4
                    if (isDense) score -= 15 // Never wear dense to gym
                }
                Microclimate.UNKNOWN -> {
                    if (tags.contains("ASSINATURA") || tags.contains("DIA A DIA")) score += 5
                    if (isFresh && temp > 25f) score += 3
                    if (isDense && temp < 20f) score += 3
                }
            }
"""
content = content.replace(old_when, new_when)

old_reasoning = """
    private fun generateReasoning(perfume: PerfumeEntity, weather: WeatherData, microclimate: Microclimate): String {
        return when (microclimate) {
            Microclimate.OFFICE_AC -> "Mesmo com ${weather.temperature.toInt()}ºC lá fora, o ar-condicionado pede a elegância controlada de ${perfume.name}."
            Microclimate.MALL_MILD -> "Versátil e na medida certa para um passeio em clima ameno."
            Microclimate.OUTDOOR_HOT -> "Fresco e leve. Não vai desandar no calor de ${weather.temperature.toInt()}ºC."
            Microclimate.DATE_NIGHT -> "Intrigante e noturno. Perfeito para um encontro."
        }
    }
"""

new_reasoning = """
    private fun generateReasoning(perfume: PerfumeEntity, weather: WeatherData, microclimate: Microclimate): String {
        return when (microclimate) {
            Microclimate.OFFICE_AC -> "Mesmo com ${weather.temperature.toInt()}ºC lá fora, o ar-condicionado pede a elegância controlada de ${perfume.name}."
            Microclimate.MALL_MILD -> "Versátil e na medida certa para um passeio em clima ameno."
            Microclimate.OUTDOOR_HOT -> "Fresco e leve. Não vai desandar no clima de ${weather.temperature.toInt()}ºC de ${weather.city.split(",")[0]}."
            Microclimate.DATE_NIGHT -> "Intrigante e sedutor. Perfeito para um encontro."
            Microclimate.CLUB_PARTY -> "Potente e chamativo. Feito para cortar o ar quente e se destacar."
            Microclimate.GYM -> "Limpo, fresco e inofensivo. Traz energia sem sufocar ninguém ao redor."
            Microclimate.UNKNOWN -> "A escolha mais segura e versátil da sua coleção para a temperatura de hoje."
        }
    }
"""
content = content.replace(old_reasoning, new_reasoning)


with open("app/src/main/java/com/aistudio/perfumatico/utils/OracleEngine.kt", "w") as f:
    f.write(content)
