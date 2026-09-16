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
        "limao" -> "lemon fruit"
        "maca" -> "red apple fruit"
        "especiarias" -> "mixed spices and herbs"
        "notas_amadeiradas" -> "wood logs and bark"
        "citricos" -> "mixed citrus fruits"
        "madeiras" -> "wood logs"
        "musgo" -> "green moss"
        "pimenta" -> "black peppercorns"
        "ambar_cinzento" -> "ambergris"
        "notas_florais" -> "mixed beautiful flowers"
        "laranja" -> "orange fruit"
        "baunilha_bourbon" -> "bourbon vanilla beans"
        "notas_verdes" -> "green leaves and foliage"
        "cassis" -> "blackcurrant berries"
        "groselha_preta" -> "blackcurrant berries"
        "salvia_esclareia" -> "clary sage herb"
        "madeira_guaiac" -> "guaiac wood"
        "madeira_de_cashmere" -> "cashmere wool texture"
        "acorde_gourmand" -> "sweet caramel chocolate dessert"
        "frutas_citricas" -> "citrus fruits"
        "madeira_de_cedro" -> "cedar wood"
        "madeira_de_sandalo" -> "sandalwood"
        "flor_de_iris" -> "iris flower"
        "flores_brancas" -> "white flowers"
        "sal_marinho" -> "sea salt crystals"
        "amendoa_amarga" -> "bitter almond"
        "madeiras_claras" -> "light colored wood"
        "notas_marinhas" -> "sea water waves"
        "madeira_de_ambar" -> "amber wood chunk"
        "folha_de_violeta" -> "violet leaves"
        "madeira_de_guaiac" -> "guaiac wood"
        "zimbro" -> "juniper berries"
        "castanha" -> "chestnut nut"
        "notas_frutadas" -> "mixed tropical fruits"
        "agarwood_oud" -> "agarwood piece"
        "laranja_sanguinea" -> "blood orange slice"
        "carvalho" -> "oak wood"
        "cominho" -> "cumin seeds"
        "copaiba" -> "copaiba resin"
        "madeiras_brancas" -> "white wood logs"
        "madeiras_escuras" -> "dark wood logs"
        "artemisia" -> "artemisia herb"
        "ambreta" -> "ambrette seeds"
        "folha_de_tabaco" -> "dried tobacco leaves"
        "bagas_de_zimbro" -> "juniper berries"
        "notas_oceanicas" -> "ocean breeze water splash"
        "lucia_lima" -> "lemon verbena leaves"
        "acucar" -> "sugar cubes"
        "alcacuz" -> "licorice root"
        "trufa" -> "black truffle"
        "mandarina_verde" -> "green mandarin orange"
        "resinas" -> "mixed natural resins"
        "notas_especiadas" -> "mixed spices and herbs"
        "tomilho" -> "thyme herb"
        "angelica" -> "angelica root"
        "cravo" -> "clove spice"
        "tamaras" -> "date fruit"
        "musk_branco" -> "white musk powder"
        "avela" -> "hazelnut"
        "laranja_amarga" -> "bitter orange"
        "toffee" -> "caramel toffee"
        "frutas_secas" -> "dried fruits mix"
        "fumaca" -> "wispy smoke"
        "priprioca" -> "priprioca root"
        "groselha" -> "redcurrant berries"
        "frutas_vermelhas" -> "mixed red berries"
        "anis" -> "star anise spice"
        "bambu" -> "green bamboo stalks"
        "notas_solares" -> "bright glowing sun rays"
        "notas_metalicas" -> "shiny silver metal texture"
        "areia" -> "golden beach sand"
        "polvora" -> "gunpowder sparks"
        "leite" -> "splash of fresh milk"
        "cannabis" -> "green hemp leaves"
        "canhamo" -> "green hemp leaves"
        "tinta" -> "black ink drop"
        "agave" -> "green agave plant"
        "cacto" -> "green cactus"
        "gin" -> "gin cocktail glass"
        "vinho" -> "glass of red wine"
        "sal" -> "sea salt crystals"
        "algodao" -> "soft white cotton"
        "muguet" -> "lily of the valley flower"
        "tiare" -> "tiare flower"
        "flor_de_tiare" -> "tiare flower"
        "frangipani" -> "frangipani flower"
        "jasmim_manga" -> "frangipani flower"
        "orquidea_negra" -> "dark black orchid flower"
        "ebano" -> "dark ebony wood"
        "madeira_de_ebano" -> "dark ebony wood"
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

fun levenshtein(s1: String, s2: String): Int {
    if (s1 == s2) return 0
    if (s1.isEmpty()) return s2.length
    if (s2.isEmpty()) return s1.length
    var v0 = IntArray(s2.length + 1) { it }
    var v1 = IntArray(s2.length + 1)
    for (i in s1.indices) {
        v1[0] = i + 1
        for (j in s2.indices) {
            val cost = if (s1[i] == s2[j]) 0 else 1
            v1[j + 1] = minOf(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost)
        }
        for (j in 0..s2.length) {
            v0[j] = v1[j]
        }
    }
    return v1[s2.length]
}

fun PerfumeEntity.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    val cleanQuery = query.unaccentAndNormalize()
    val targetText = "$name $brand $family $notes $referenceName $tags".unaccentAndNormalize()
    
    // Direct match
    if (targetText.contains(cleanQuery)) return true
    
    // Alias overrides
    val aliasQuery = when {
        cleanQuery.contains("lataffa") -> cleanQuery.replace("lataffa", "lattafa")
        cleanQuery.contains("boticario") -> cleanQuery.replace("boticario", "o boticario")
        cleanQuery.contains("rabane") -> cleanQuery.replace("rabane", "rabanne")
        else -> null
    }
    if (aliasQuery != null && targetText.contains(aliasQuery)) return true
    
    // Multi-word search with fuzzy matching
    val queryWords = cleanQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
    val targetWords = targetText.split(Regex("\\s+")).filter { it.isNotBlank() }
    
    if (queryWords.isNotEmpty()) {
        val allMatch = queryWords.all { qWord ->
            val aliasQWord = if (qWord == "lataffa") "lattafa" else qWord
            if (targetText.contains(aliasQWord)) return@all true
            
            // Fuzzy match logic: allow 1 typo for words > 3 chars, 2 typos for words > 5 chars
            val maxDistance = when {
                aliasQWord.length <= 3 -> 0
                aliasQWord.length <= 5 -> 1
                else -> 2
            }
            
            if (maxDistance > 0) {
                targetWords.any { tWord -> 
                    levenshtein(aliasQWord, tWord) <= maxDistance 
                }
            } else {
                false
            }
        }
        if (allMatch) return true
    }
    
    return false
}
