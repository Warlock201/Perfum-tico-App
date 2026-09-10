package com.aistudio.perfumatico.utils

import androidx.compose.ui.graphics.Color
import com.aistudio.perfumatico.data.local.PerfumeEntity
import kotlin.math.roundToInt

data class Accord(
    val name: String,
    val color: Color,
    val percentage: Int
)

object AccordsEngine {

    // Define colors for each accord
    private val C_CITRICO = Color(0xFFFFD54F) // Yellow
    private val C_AMADEIRADO = Color(0xFF795548) // Brown
    private val C_FLORAL = Color(0xFFF48FB1) // Pink
    private val C_DOCE = Color(0xFFE91E63) // Deep Pink
    private val C_ESPECIADO_QUENTE = Color(0xFFF4511E) // Deep Orange
    private val C_ESPECIADO_FRESCO = Color(0xFFFF9800) // Orange
    private val C_AROMATICO = Color(0xFF4DB6AC) // Teal/Green
    private val C_FRUTADO = Color(0xFFCE93D8) // Purple
    private val C_COURO = Color(0xFF3E2723) // Dark Brown
    private val C_AQUATICO = Color(0xFF4FC3F7) // Light Blue
    private val C_ALMISCARADO = Color(0xFFBDBDBD) // Grey
    private val C_VERDE = Color(0xFF81C784) // Light Green
    private val C_TERROSO = Color(0xFF5D4037) // Earthy
    private val C_TABACO = Color(0xFF8D6E63) // Tobacco

    private val noteToAccordMap = mapOf(
        // Cítricos
        "limão" to "Cítrico", "bergamota" to "Cítrico", "toranja" to "Cítrico", "laranja" to "Cítrico",
        "mandarina" to "Cítrico", "yuzu" to "Cítrico", "cidra" to "Cítrico", "tangerina" to "Cítrico",
        "limão siciliano" to "Cítrico", "petitgrain" to "Cítrico", "lima" to "Cítrico",
        
        // Amadeirados
        "cedro" to "Amadeirado", "sândalo" to "Amadeirado", "vetiver" to "Amadeirado", 
        "patchouli" to "Amadeirado", "oud" to "Amadeirado", "musgo" to "Amadeirado", 
        "musgo de carvalho" to "Amadeirado", "guaiac" to "Amadeirado", "madeira" to "Amadeirado",
        "pau-brasil" to "Amadeirado", "mogno" to "Amadeirado", "pinheiro" to "Amadeirado",
        "madeira de caxemira" to "Amadeirado", "agarwood" to "Amadeirado", "cipreste" to "Amadeirado",
        
        // Florais
        "rosa" to "Floral", "jasmim" to "Floral", "íris" to "Floral", "néroli" to "Floral",
        "ylang-ylang" to "Floral", "tuberosa" to "Floral", "gerânio" to "Floral", 
        "flor de laranjeira" to "Floral", "magnólia" to "Floral", "peônia" to "Floral",
        "lírio" to "Floral", "frésia" to "Floral", "violeta" to "Floral", "orquídea" to "Floral",
        "osmanthus" to "Floral", "flor de maçã" to "Floral",
        
        // Doces / Gourmands
        "baunilha" to "Doce", "fava tonka" to "Doce", "caramelo" to "Doce", "pralinê" to "Doce",
        "cacau" to "Doce", "mel" to "Doce", "açúcar" to "Doce", "chocolate" to "Doce",
        "marshmallow" to "Doce", "algodão doce" to "Doce", "fava de baunilha" to "Doce",
        "cumarina" to "Doce", "café" to "Doce",
        
        // Especiado Quente
        "canela" to "Especiado Quente", "noz-moscada" to "Especiado Quente", "cardamomo" to "Especiado Quente",
        "pimenta preta" to "Especiado Quente", "cravo" to "Especiado Quente", "especiarias" to "Especiado Quente",
        "anis" to "Especiado Quente", "anis estrelado" to "Especiado Quente", "cominho" to "Especiado Quente",
        
        // Especiado Fresco
        "pimenta rosa" to "Especiado Fresco", "gengibre" to "Especiado Fresco", "coentro" to "Especiado Fresco",
        "zimbro" to "Especiado Fresco", "pimenta branca" to "Especiado Fresco",
        
        // Aromático
        "lavanda" to "Aromático", "sálvia" to "Aromático", "alecrim" to "Aromático",
        "hortelã" to "Aromático", "manjericão" to "Aromático", "tomilho" to "Aromático",
        "artemísia" to "Aromático", "estragão" to "Aromático",
        
        // Frutado
        "maçã" to "Frutado", "abacaxi" to "Frutado", "pêssego" to "Frutado", "coco" to "Frutado",
        "morango" to "Frutado", "cereja" to "Frutado", "ameixa" to "Frutado", "pera" to "Frutado",
        "framboesa" to "Frutado", "amora" to "Frutado", "cassis" to "Frutado", "melão" to "Frutado",
        "figo" to "Frutado", "manga" to "Frutado", "maracujá" to "Frutado", "groselha" to "Frutado",
        
        // Couro / Tabaco
        "couro" to "Couro", "camurça" to "Couro", "tabaco" to "Tabaco",
        
        // Aquático
        "notas oceânicas" to "Aquático", "sal marinho" to "Aquático", "calone" to "Aquático",
        "água" to "Aquático", "notas aquáticas" to "Aquático", "ambroxan" to "Aquático",
        
        // Almiscarado
        "almíscar" to "Almiscarado", "musk" to "Almiscarado", "âmbar" to "Almiscarado", 
        "âmbar cinzento" to "Almiscarado", "almíscar branco" to "Almiscarado", "cashmeran" to "Almiscarado",
        
        // Verde
        "folhas verdes" to "Verde", "gálbano" to "Verde", "chá verde" to "Verde",
        "chá" to "Verde", "notas verdes" to "Verde", "mate" to "Verde",
        
        // Terroso
        "raiz de orris" to "Terroso", "notas terrosas" to "Terroso", "trufa" to "Terroso"
    )

    private val accordColors = mapOf(
        "Cítrico" to C_CITRICO,
        "Amadeirado" to C_AMADEIRADO,
        "Floral" to C_FLORAL,
        "Doce" to C_DOCE,
        "Especiado Quente" to C_ESPECIADO_QUENTE,
        "Especiado Fresco" to C_ESPECIADO_FRESCO,
        "Aromático" to C_AROMATICO,
        "Frutado" to C_FRUTADO,
        "Couro" to C_COURO,
        "Tabaco" to C_TABACO,
        "Aquático" to C_AQUATICO,
        "Almiscarado" to C_ALMISCARADO,
        "Verde" to C_VERDE,
        "Terroso" to C_TERROSO
    )

    fun calculateAccords(perfume: PerfumeEntity): List<Accord> {
        val accordScores = mutableMapOf<String, Float>()
        
        fun processNotes(notesStr: String, weight: Float) {
            if (notesStr.isBlank()) return
            val notesList = notesStr.split("|").map { it.trim().lowercase() }.filter { it.isNotBlank() }
            for (n in notesList) {
                // Find matching accord. Exact match first, then contains.
                val matchedAccord = noteToAccordMap[n] ?: noteToAccordMap.entries.firstOrNull { n.contains(it.key) }?.value
                
                if (matchedAccord != null) {
                    accordScores[matchedAccord] = (accordScores[matchedAccord] ?: 0f) + weight
                } else {
                    // Fallback heuristics
                    if (n.contains("madeira") || n.contains("wood")) {
                        accordScores["Amadeirado"] = (accordScores["Amadeirado"] ?: 0f) + weight
                    } else if (n.contains("flor") || n.contains("flower")) {
                        accordScores["Floral"] = (accordScores["Floral"] ?: 0f) + weight
                    } else if (n.contains("âmbar") || n.contains("ambar")) {
                        accordScores["Almiscarado"] = (accordScores["Almiscarado"] ?: 0f) + weight
                    }
                }
            }
        }

        // Apply weights based on pyramid position (Base notes last longer, often define the core accord)
        // However, top notes are usually the most prominent initially. Let's give them fairly equal weights, 
        // with base slightly heavier.
        processNotes(perfume.topNotes, 1.0f)
        processNotes(perfume.heartNotes, 1.2f)
        processNotes(perfume.baseNotes, 1.5f)
        
        // Fallback to all notes if pyramid is empty
        if (perfume.topNotes.isBlank() && perfume.heartNotes.isBlank() && perfume.baseNotes.isBlank()) {
            processNotes(perfume.notes, 1.0f)
        }

        val totalScore = accordScores.values.sum()
        if (totalScore == 0f) return emptyList()

        return accordScores.entries
            .map { (name, score) ->
                val percentage = ((score / totalScore) * 100).roundToInt()
                val color = accordColors[name] ?: Color.Gray
                Accord(name, color, percentage)
            }
            .filter { it.percentage > 0 }
            .sortedByDescending { it.percentage }
            .take(6) // Max 6 accords
    }
}
