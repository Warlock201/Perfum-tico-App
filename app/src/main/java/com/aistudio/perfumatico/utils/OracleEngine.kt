package com.aistudio.perfumatico.utils

import com.aistudio.perfumatico.data.local.PerfumeEntity
import kotlin.math.abs

data class OracleRecommendation(
    val primaryPerfume: PerfumeEntity,
    val alternativePerfume: PerfumeEntity?,
    val reasoning: String
)

object OracleEngine {

    enum class Microclimate {
        OFFICE_AC,      // Ar condicionado forte
        MALL_MILD,      // Shopping/Clima Ameno
        OUTDOOR_HOT,    // Ar livre/Calor
        DATE_NIGHT      // Encontro à noite
    }

    fun recommend(
        weather: WeatherData,
        microclimate: Microclimate,
        collection: List<PerfumeEntity>
    ): OracleRecommendation? {
        val available = collection.filter { it.status == "Já possuo" }
        if (available.isEmpty()) return null

        val scoredPerfumes = available.map { perfume ->
            var score = 0
            
            val tags = perfume.tags.map { it.uppercase() }
            val isFresh = tags.contains("CALOR") || tags.contains("DIA A DIA") || perfume.family.contains("Cítrico", true) || perfume.family.contains("Aquático", true)
            val isDense = tags.contains("FRIO") || tags.contains("NOITE") || tags.contains("BALADA") || perfume.family.contains("Oriental", true) || perfume.family.contains("Couro", true)
            
            // Baseline weather logic
            val temp = weather.temperature
            
            when (microclimate) {
                Microclimate.OFFICE_AC -> {
                    // Office AC neutralizes outside heat. Favor versatile, elegant, not too loud.
                    if (tags.contains("TRABALHO") || tags.contains("ASSINATURA")) score += 5
                    if (isDense && temp > 25f) score += 2 // Can wear dense if AC is strong, but slightly less than if it was naturally cold
                    if (isDense && temp <= 25f) score += 4
                    if (perfume.projection > 8) score -= 3 // Too loud for office
                }
                Microclimate.MALL_MILD -> {
                    // Mild indoor climate. Very versatile.
                    if (tags.contains("SHOPPING") || tags.contains("ASSINATURA")) score += 5
                    if (isFresh) score += 2
                    if (isDense && temp < 28f) score += 2
                }
                Microclimate.OUTDOOR_HOT -> {
                    // Hot and humid outdoors. Strict rules.
                    if (tags.contains("CALOR") || tags.contains("PRAIA") || tags.contains("ACADEMIA")) score += 5
                    if (isFresh) score += 5
                    if (isDense) score -= 10 // Ban dense fragrances
                    if (weather.humidity > 70) score -= 2 // High humidity makes everything heavier
                }
                Microclimate.DATE_NIGHT -> {
                    // Date night.
                    if (tags.contains("ENCONTRO") || tags.contains("NOITE")) score += 6
                    if (!weather.isDay) score += 2
                    if (temp > 27f && isDense) score -= 3 // Hot night, careful with dense
                    if (temp <= 27f && isDense) score += 4 // Cool night, perfect for dense
                }
            }
            
            // User preference tie-breaker
            if (perfume.userPreference == 1) score += 2 // Amo
            if (perfume.userPreference == 2) score += 1 // Gosto

            Pair(perfume, score)
        }.sortedByDescending { it.second }

        if (scoredPerfumes.isEmpty()) return null

        val primary = scoredPerfumes[0].first
        val alternative = if (scoredPerfumes.size > 1) scoredPerfumes[1].first else null
        
        val reasoning = generateReasoning(primary, weather, microclimate)

        return OracleRecommendation(primary, alternative, reasoning)
    }

    private fun generateReasoning(perfume: PerfumeEntity, weather: WeatherData, microclimate: Microclimate): String {
        return when (microclimate) {
            Microclimate.OFFICE_AC -> "Mesmo com ${weather.temperature.toInt()}ºC lá fora, o ar-condicionado pede a elegância controlada de ${perfume.name}."
            Microclimate.MALL_MILD -> "Versátil e na medida certa para um passeio em clima ameno."
            Microclimate.OUTDOOR_HOT -> "Fresco e leve. Não vai desandar no calor de ${weather.temperature.toInt()}ºC."
            Microclimate.DATE_NIGHT -> "Intrigante e noturno. Perfeito para um encontro."
        }
    }
}
