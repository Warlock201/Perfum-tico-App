package com.aistudio.perfumatico.utils

import android.content.Context
import java.text.Normalizer
import com.aistudio.perfumatico.data.local.PerfumeEntity

fun String.cleanNotePrefix(): String {
    return this.trim()
        .replace(Regex("^(sa[ií]da|topo|top|cora[cç][aã]o|coracao|heart|fundo|base)\\s*:\\s*", RegexOption.IGNORE_CASE), "")
        .trim()
}

fun String.normalizeNoteName(): String {
    val clean = this.cleanNotePrefix()
    val normalized = Normalizer.normalize(clean, Normalizer.Form.NFD)
    val noAccents = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    return noAccents.lowercase()
        .replace(Regex("[^a-z0-9_]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')
}

fun String.getNoteImageUrl(): String {
    val normalized = this.normalizeNoteName()
    val englishName = when (normalized) {
        "maca_verde" -> "green apple fruit"
        "abacaxi" -> "pineapple fruit"
        "pessego" -> "peach fruit"
        "melancia" -> "watermelon slice"
        "limao_siciliano" -> "sicilian lemon fruit"
        "mandarina" -> "mandarin orange fruit"
        "toranja" -> "grapefruit slice"
        "laranja_doce" -> "sweet orange fruit"
        "bergamota" -> "bergamot orange"
        "rosa" -> "red rose flower"
        "jasmim" -> "jasmine flower"
        "lavanda" -> "lavender flower"
        "neroli" -> "neroli orange blossom"
        "iris" -> "iris flower"
        "geranio" -> "geranium flower"
        "flor_de_laranjeira" -> "orange blossom flower"
        "tuberosa" -> "tuberose flower"
        "sandalo" -> "sandalwood"
        "cedro" -> "cedar wood"
        "vetiver" -> "vetiver grass roots"
        "patchouli" -> "patchouli leaves"
        "oud" -> "agarwood piece"
        "madeira_gaiac" -> "guaiac wood"
        "pimenta_rosa" -> "pink peppercorns"
        "pimenta_preta" -> "black peppercorns"
        "cardamomo" -> "cardamom pods"
        "canela" -> "cinnamon sticks"
        "noz_moscada" -> "nutmeg spice"
        "cravo_da_india" -> "clove spice"
        "acafrao" -> "saffron threads"
        "baunilha" -> "vanilla bean pod"
        "fava_tonka" -> "tonka bean"
        "caramelo" -> "caramel candy"
        "cacau" -> "cocoa beans"
        "cafe" -> "roasted coffee beans"
        "ambar" -> "amber resin"
        "incenso" -> "burning incense sticks"
        "musk" -> "white musk powder"
        "almiscar" -> "white musk powder"
        "couro" -> "suede leather texture"
        "notas_aquaticas" -> "water splash"
        "lima" -> "lime wedge"
        "yuzu" -> "yuzu fruit"
        "tangerina" -> "tangerine fruit"
        "cereja" -> "cherry fruit"
        "ameixa" -> "plum fruit"
        "coco" -> "coconut"
        "figo" -> "fig fruit"
        "pera" -> "pear fruit"
        "amora" -> "blackberry fruit"
        "framboesa" -> "raspberry fruit"
        "cassis_groselha_preta" -> "blackcurrant berries"
        "maracuja" -> "passion fruit"
        "manga" -> "mango fruit"
        "ylang_ylang" -> "ylang ylang flower"
        "magnolia" -> "magnolia flower"
        "peonia" -> "peony flower"
        "lirio_do_vale" -> "lily of the valley flower"
        "orquidea" -> "orchid flower"
        "fresia" -> "freesia flower"
        "violeta" -> "violet flower"
        "menta" -> "mint leaves"
        "hortela" -> "mint leaves"
        "manjericao" -> "basil leaves"
        "salvia" -> "sage herb"
        "alecrim" -> "rosemary herb"
        "cha_verde" -> "green tea leaves"
        "cha_preto" -> "black tea leaves"
        "absinto" -> "wormwood plant leaves"
        "galbano" -> "galbanum resin"
        "folhas_de_violeta" -> "violet leaves"
        "eucalipto" -> "eucalyptus leaves"
        "cipreste" -> "cypress branch"
        "junipero_zimbro" -> "juniper berries"
        "pinheiro" -> "pine cones and needles"
        "musgo_de_carvalho" -> "oakmoss"
        "mirra" -> "myrrh resin"
        "olibano" -> "frankincense resin"
        "benjoim" -> "benzoin resin"
        "ladano" -> "labdanum resin"
        "ambroxan" -> "white ambergris crystal"
        "aldeidos" -> "sparkling bubbles"
        "praline" -> "praline chocolate"
        "mel" -> "honey dripping"
        "amendoa" -> "almond nut"
        "rum" -> "rum liquor splash"
        "conhaque" -> "cognac liquor splash"
        "tabaco" -> "dried tobacco leaves"
        "chocolate" -> "chocolate bar"
        "chocolate_branco" -> "white chocolate chunks"
        "gengibre" -> "ginger root"
        "petitgrain" -> "petitgrain leaves"
        "lichia" -> "lychee fruit"
        "melao" -> "melon slice"
        "pistache" -> "pistachio nut"
        "algodao_doce" -> "pink cotton candy"
        "marshmallow" -> "marshmallows"
        "heliotropio" -> "heliotrope flower"
        "osmanthus" -> "osmanthus flower"
        "camomila" -> "chamomile flower"
        "lotus" -> "lotus flower"
        "camurca" -> "suede leather"
        "betula" -> "birch wood bark"
        "pau_rosa" -> "rosewood piece"
        "cashmeran" -> "cashmere wool texture"
        "iso_e_super" -> "clear essential oil drop"
        "anis_estrelado" -> "star anise spice"
        "coentro" -> "coriander seeds"
        "elemi" -> "elemi resin chunk"
        "champanhe" -> "champagne glass splashing"
        "notas_ozonicas" -> "fresh water breeze splash"
        else -> normalized.replace("_", " ")
    }
    
    val prompt = "Macro photography of $englishName, botanical perfume ingredient, no people, no faces, no humans, isolated on a solid dark gray background, highly detailed"
    val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8").replace("+", "%20")
    val seed = kotlin.math.abs(englishName.hashCode())
    
    return "https://image.pollinations.ai/prompt/$encodedPrompt?width=200&height=200&nologo=true&seed=$seed&v=2"
}

fun Context.getNoteDrawableResId(noteName: String): Int {
    val norm = noteName.normalizeNoteName()
    return this.resources.getIdentifier("note_$norm", "drawable", this.packageName)
}

fun String.unaccentAndNormalize(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
        .trim()
}

fun PerfumeEntity.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    val cleanQuery = query.unaccentAndNormalize()
    val targetText = "$name $brand $family $notes $referenceName $tags".unaccentAndNormalize()
    
    // Direct match
    if (targetText.contains(cleanQuery)) return true
    
    // Common brand alias and typo replacements (e.g. "lataffa" -> "lattafa")
    val aliasQuery = when {
        cleanQuery.contains("lataffa") -> cleanQuery.replace("lataffa", "lattafa")
        cleanQuery.contains("lattafa") -> cleanQuery.replace("lattafa", "lataffa")
        cleanQuery.contains("boticario") -> cleanQuery.replace("boticario", "o boticario")
        cleanQuery.contains("rabane") -> cleanQuery.replace("rabane", "rabanne")
        else -> null
    }
    if (aliasQuery != null && targetText.contains(aliasQuery)) return true
    
    // Multi-word search
    val words = cleanQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.size > 1) {
        val allMatch = words.all { w ->
            targetText.contains(w) || (w == "lataffa" && targetText.contains("lattafa"))
        }
        if (allMatch) return true
    }
    
    return false
}
