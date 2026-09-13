package com.aistudio.perfumatico.data.model

data class OlfactoryNote(
    val id: String = "",
    val nome: String = "",
    val imageUrl: String = ""
)

data class Bottle(
    val size: String = "100ml",
    val quantity: Int = 1
)

enum class PerfumeStatus(val label: String) {
    HAVE("Já possuo"),
    PIPELINE("Pipeline"),
    WISH("Desejos");

    companion object {
        fun fromLabel(label: String): PerfumeStatus {
            return when (label.trim().uppercase()) {
                "JÁ POSSUO", "TENHO", "HAVING", "HAVE" -> HAVE
                "PIPELINE", "VOU COMPRAR" -> PIPELINE
                else -> WISH
            }
        }
    }
}

val AVAILABLE_TAGS = listOf(
    "DIA A DIA",
    "ASSINATURA",
    "ENCONTRO",
    "BALADA",
    "CALOR",
    "FRIO",
    "TRABALHO",
    "DORMIR",
    "ACADEMIA",
    "NOITE",
    "SHOPPING",
    "PRAIA",
    "EVENTO FORMAL"
)

val AVAILABLE_VOLUMES = listOf(
    "200ml",
    "150ml",
    "125ml",
    "100ml",
    "90ml",
    "75ml",
    "50ml",
    "30ml",
    "15ml",
    "Decant 20ml",
    "Decant 10ml",
    "Decant 5ml",
    "Amostra"
)

val AVAILABLE_FAMILIES = listOf(
    "Fresco",
    "Amadeirado",
    "Oriental",
    "Aromático",
    "Cítrico",
    "Gourmand",
    "Floral",
    "Chypre",
    "Fougère",
    "Couro",
    "Aquático",
    "Agradável Comercial",
    "Outros"
)
