import java.io.File
import java.net.URL
import java.net.HttpURLConnection
import java.net.URLEncoder

fun main() {
    val notes = listOf(
        "BERGAMOTA", "LIMÃO SICILIANO", "MANDARINA", "TORANJA", "LARANJA DOCE", "MAÇÃ VERDE", "ABACAXI", "PÊSSEGO", "MELANCIA", "ROSA", "JASMIM", "LAVANDA", "NÉROLI", "ÍRIS", "GERÂNIO", "FLOR DE LARANJEIRA", "TUBEROSA", "SÂNDALO", "CEDRO", "VETIVER", "PATCHOULI", "OUD", "MADEIRA GAIAC", "PIMENTA ROSA", "PIMENTA PRETA", "CARDAMOMO", "CANELA", "NOZ-MOSCADA", "CRAVO-DA-ÍNDIA", "AÇAFRÃO", "BAUNILHA", "FAVA TONKA", "CARAMELO", "CACAU", "CAFÉ", "ÂMBAR", "INCENSO", "MUSK", "COURO", "NOTAS AQUÁTICAS", "LIMA", "YUZU", "TANGERINA", "CEREJA", "AMEIXA", "COCO", "FIGO", "PÊRA", "AMORA", "FRAMBOESA", "CASSIS (GROSELHA PRETA)", "MARACUJÁ", "MANGA", "YLANG-YLANG", "MAGNÓLIA", "PEÔNIA", "LÍRIO-DO-VALE", "ORQUÍDEA", "FRÉSIA", "VIOLETA", "MENTA", "MANJERICÃO", "SÁLVIA", "ALECRIM", "CHÁ VERDE", "CHÁ PRETO", "ABSINTO", "GÁLBANO", "FOLHAS DE VIOLETA", "EUCALIPTO", "CIPRESTE", "JUNÍPERO (ZIMBRO)", "PINHEIRO", "MUSGO DE CARVALHO", "MIRRA", "OLÍBANO", "BENJOIM", "LÁDANO", "AMBROXAN", "ALDEÍDOS", "PRALINÊ", "MEL", "AMÊNDOA", "RUM", "CONHAQUE", "TABACO", "CHOCOLATE", "CHOCOLATE BRANCO", "GENGIBRE", "PETITGRAIN", "LICHIA", "MELÃO", "PISTACHE", "ALGODÃO DOCE", "MARSHMALLOW", "HELIOTRÓPIO", "OSMANTHUS", "CAMOMILA", "LÓTUS", "CAMURÇA", "BÉTULA", "PAU-ROSA", "CASHMERAN", "ISO E SUPER", "ANIS ESTRELADO", "COENTRO", "ELEMI", "CHAMPANHE", "NOTAS OZÔNICAS"
    )

    fun normalizeNoteName(name: String): String {
        val normalized = java.text.Normalizer.normalize(name.trim(), java.text.Normalizer.Form.NFD)
        val noAccents = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return noAccents.lowercase().replace(" ", "_")
    }
    
    fun getEnglishName(normalized: String): String {
        val noUnderscore = normalized.replace("_", " ")
        return when {
            "maca" in noUnderscore -> "apple fruit"
            "lavanda" in noUnderscore -> "lavender flower"
            "melancia" in noUnderscore -> "watermelon slice"
            "cedro" in noUnderscore -> "cedar wood"
            "ambar" in noUnderscore -> "amber resin"
            "sandalo" in noUnderscore -> "sandalwood"
            "bergamota" in noUnderscore -> "bergamot orange"
            "couro" in noUnderscore -> "leather texture"
            "baunilha" in noUnderscore -> "vanilla bean pod"
            "limao" in noUnderscore -> "lemon fruit"
            "pimenta" in noUnderscore -> "black pepper spice"
            "jasmim" in noUnderscore -> "jasmine flower"
            "rosa" in noUnderscore -> "red rose flower"
            "vetiver" in noUnderscore -> "vetiver grass roots"
            "hortela" in noUnderscore || "menta" in noUnderscore -> "mint leaves"
            "canela" in noUnderscore -> "cinnamon sticks"
            "patchouli" in noUnderscore -> "patchouli leaves"
            "fava tonka" in noUnderscore || "tonka" in noUnderscore -> "tonka bean"
            "cardamomo" in noUnderscore -> "cardamom pods"
            "neroli" in noUnderscore || "flor de laranjeira" in noUnderscore -> "neroli orange blossom"
            "oud" in noUnderscore || "agarwood" in noUnderscore -> "agarwood oud"
            "abacaxi" in noUnderscore -> "pineapple fruit"
            "ameixa" in noUnderscore -> "plum fruit"
            "cereja" in noUnderscore -> "cherry fruit"
            "coco" in noUnderscore -> "coconut"
            "framboesa" in noUnderscore -> "raspberry fruit"
            "morango" in noUnderscore -> "strawberry fruit"
            "pessego" in noUnderscore -> "peach fruit"
            "iris" in noUnderscore -> "iris flower"
            "lirio" in noUnderscore -> "lily flower"
            "geranio" in noUnderscore -> "geranium flower"
            "violeta" in noUnderscore -> "violet flower"
            "tabaco" in noUnderscore -> "tobacco leaves"
            "almiscar" in noUnderscore || "musk" in noUnderscore -> "white musk powder"
            "cafe" in noUnderscore -> "roasted coffee beans"
            "cacau" in noUnderscore || "chocolate" in noUnderscore -> "cocoa beans"
            "incenso" in noUnderscore -> "burning incense smoke"
            "noz moscada" in noUnderscore -> "nutmeg spice"
            "toranja" in noUnderscore || "grapefruit" in noUnderscore -> "grapefruit slice"
            "laranja" in noUnderscore -> "orange fruit slice"
            "mandarina" in noUnderscore || "tangerina" in noUnderscore -> "tangerine fruit"
            "gengibre" in noUnderscore -> "ginger root"
            "noz" in noUnderscore || "amendoa" in noUnderscore -> "almond nut"
            "cha" in noUnderscore || "tea" in noUnderscore -> "green tea leaves"
            "salvia" in noUnderscore -> "sage herb"
            "alecrim" in noUnderscore -> "rosemary herb"
            "ylang" in noUnderscore -> "ylang ylang flower"
            "cravo" in noUnderscore -> "clove spice"
            "pistache" in noUnderscore -> "pistachio nut"
            "mel" in noUnderscore -> "honey dripping"
            "notas aquaticas" in noUnderscore || "aquatic" in noUnderscore -> "water splash splash"
            "lima" in noUnderscore || "lime" in noUnderscore -> "lime wedge"
            "yuzu" in noUnderscore -> "yuzu fruit"
            "madeira gaiac" in noUnderscore || "guaiac" in noUnderscore -> "guaiac wood"
            "acafrao" in noUnderscore || "saffron" in noUnderscore -> "saffron threads"
            else -> noUnderscore
        }
    }

    val destDir = File("app/src/main/res/drawable-nodpi")
    if (!destDir.exists()) destDir.mkdirs()

    for (note in notes) {
        val norm = normalizeNoteName(note)
        val engName = getEnglishName(norm)
        val prompt = "A single $engName, centered, isolated on a solid dark gray background, macro photography, soft studio lighting, highly detailed"
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8").replace("+", "%20")
        val seed = kotlin.math.abs(engName.hashCode())
        val urlStr = "https://image.pollinations.ai/prompt/$encodedPrompt?width=200&height=200&nologo=true&seed=$seed&v=2"
        
        val destFile = File(destDir, "note_${norm.replace("-", "_").replace("(", "").replace(")", "").replace(" ", "")}.jpg")
        
        if (!destFile.exists()) {
            println("Downloading: ${destFile.name}")
            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 Perfumatico/1.0")
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                
                val inputStream = conn.inputStream
                destFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                Thread.sleep(100) // slight delay to not hammer the server too fast
            } catch (e: Exception) {
                println("Failed to download $note: ${e.message}")
            }
        } else {
            println("Already exists: ${destFile.name}")
        }
    }
}
