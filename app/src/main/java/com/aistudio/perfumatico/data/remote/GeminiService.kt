package com.aistudio.perfumatico.data.remote

import com.aistudio.perfumatico.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import retrofit2.Response

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
    val tools: List<Tool>? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

@Serializable
data class Tool(
    val googleSearch: GoogleSearch? = null
)

@Serializable
class GoogleSearch

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): Response<GenerateContentResponse>

    @POST("v1beta/models/{model}:streamGenerateContent")
    @Streaming
    suspend fun streamGenerateContent(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Query("alt") alt: String = "sse",
        @Body request: GenerateContentRequest
    ): Response<ResponseBody>
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiService {

    fun getApiKey(): String {
        val masked = BuildConfig.GEMINI_KEY_MASKED.trim()
        if (masked.isNotBlank()) {
            try {
                val decoded = masked.split(",")
                    .filter { it.isNotBlank() }
                    .map { (it.trim().toInt() xor 0x5A).toChar() }
                    .joinToString("")
                if (decoded.isNotBlank()) return decoded
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val envNew = BuildConfig.GEMINI_API_KEY_NEW.trim()
        if (envNew.isNotBlank() && !envNew.startsWith("YOUR_") && envNew != "PROTECTED") return envNew
        val envOld = BuildConfig.GEMINI_API_KEY.trim()
        if (envOld.isNotBlank() && !envOld.startsWith("YOUR_") && envOld != "PROTECTED") return envOld
        return ""
    }

    private val ACTIVE_MODELS = listOf("gemini-3.5-flash", "gemini-flash-latest")

    private suspend fun <T> executeWithFallback(
        models: List<String> = ACTIVE_MODELS,
        action: suspend (String) -> Response<T>
    ): T {
        var lastError: Exception? = null
        for (model in models) {
            try {
                val response = action(model)
                if (response.isSuccessful) {
                    return response.body()!!
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    lastError = Exception("HTTP ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw Exception("Todos os modelos falharam. Último erro: ${lastError?.message}")
    }

    suspend fun completeOlfactoryPyramid(perfumeName: String, brandName: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        
        val prompt = """
            Você é um especialista em perfumaria. Liste a pirâmide olfativa do perfume '$perfumeName' da marca '$brandName'.
            Retorne APENAS no seguinte formato:
            Saída: nota1, nota2 | Coração: nota3, nota4 | Fundo: nota5, nota6
            
            Se não souber exatamente as notas ou o perfume for muito obscuro, retorne as notas mais prováveis baseadas no nome, 
            mas o formato deve ser ESTRITAMENTE como o exemplo acima, usando o pipe (|) para separar as fases. 
            Não adicione nenhum outro texto, saudação ou explicação.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.2f)
        )
        
        try {
            val response = executeWithFallback(ACTIVE_MODELS) { model -> 
                RetrofitClient.service.generateContent(model, apiKey, request) 
            }
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("API_KEY_INVALID") || msg.contains("400")) {
                "Erro: Chave de API do Gemini inválida. Configure uma chave válida no painel de Segredos."
            } else {
                "Erro ao buscar notas com IA: ${e.message}"
            }
        }
    }
    
    suspend fun autoFillPerfume(perfumeName: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val prompt = """
            Você é o maior especialista e enciclopédia viva de perfumaria do mundo (Fragrantica, Parfumo, Basenotes).
            O usuário quer cadastrar a fragrância: "$perfumeName".
            
            Sua missão é identificar com máxima exatidão este perfume e extrair a sua pirâmide olfativa oficial completa e características.
            
            Retorne APENAS um objeto JSON ESTRITO com o seguinte formato:
            {
                "name": "Nome Oficial do Perfume",
                "brand": "Marca / Casa Oficial (ex: Dior, Chanel, Natura, O Boticário, Lattafa, Paco Rabanne, Tom Ford)",
                "family": "Família Olfativa principal (escolha entre: Fresco, Amadeirado, Oriental, Aromático, Cítrico, Gourmand, Floral, Chypre, Fougère, Couro, Aquático)",
                "topNotes": "Notas de Saída/Topo separadas por vírgula (ex: Bergamota da Calábria, Pimenta)",
                "heartNotes": "Notas de Coração/Corpo separadas por vírgula (ex: Pimenta de Szechuan, Lavanda, Pimenta Rosa, Vetiver, Patchouli, Gerânio, Elemi)",
                "baseNotes": "Notas de Fundo/Base separadas por vírgula (ex: Ambroxan, Cedro, Ládano)",
                "fixation": 8,
                "projection": 8,
                "priceMin": 250,
                "priceMax": 450,
                "referenceName": "Se for contratipo/clone de outro clássico (ex: Inspirado no Aventus), informe aqui. Se for original, deixe vazio."
            }
            
            DIRETRIZES FUNDAMENTAIS:
            1. Preencha SEMPRE as notas de saída (topNotes), coração (heartNotes) e fundo (baseNotes) com as notas oficiais conhecidas dessa fragrância.
            2. Não deixe as notas em branco.
            3. Retorne APENAS o JSON puro, sem crases, markdown ou qualquer texto adicional.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.1f, responseMimeType = "application/json")
        )

        try {
            val response = executeWithFallback(ACTIVE_MODELS) { model -> 
                RetrofitClient.service.generateContent(model, apiKey, request) 
            }
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
            val cleaned = raw.replace("```json", "").replace("```", "").trim()
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            if (start != -1 && end != -1 && end > start) {
                cleaned.substring(start, end + 1)
            } else {
                cleaned
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    suspend fun chatWithSommelier(history: List<Content>, onToken: (String) -> Unit) = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val systemInstruction = Content(
            parts = listOf(Part(text = "Você é um Sommelier de Perfumes do Perfumático. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção, você NÃO precisa perguntar em qual categoria, pois botões aparecerão na tela. Você DEVE APENAS retornar no final da sua mensagem o comando exato: [ADD_PERFUME: <Nome do Perfume> | <Marca> | <Status>]. Exemplo: [ADD_PERFUME: Homem Dom | Natura | Quero ter]" ))
        )
        
        val request = GenerateContentRequest(
            contents = history,
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val responseBody = executeWithFallback(ACTIVE_MODELS) { model -> 
                RetrofitClient.service.streamGenerateContent(model, apiKey, request = request) 
            }
            
            responseBody.source().use { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val jsonStr = line.substring(6)
                        if (jsonStr.trim() != "[DONE]") {
                            try {
                                val json = Json { ignoreUnknownKeys = true }
                                val chunk = json.decodeFromString<GenerateContentResponse>(jsonStr)
                                val text = chunk.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                                if (text.isNotEmpty()) {
                                    withContext(Dispatchers.Main) { onToken(text) }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val userFriendlyError = when {
                msg.contains("API_KEY_INVALID", ignoreCase = true) || msg.contains("API key not valid", ignoreCase = true) ->
                    "⚠️ Chave de API do Gemini inválida.\n\nA chave atual não foi reconhecida pelo Google.\n\n👉 Configure sua chave corretamente nas variáveis de ambiente (Secrets) do projeto."
                msg.contains("API_KEY_SERVICE_BLOCKED", ignoreCase = true) ->
                    "⚠️ Chave bloqueada para a API Gemini (Generative Language API)."
                msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) || msg.contains("429") ->
                    "⚠️ Limite temporário de requisições do Gemini atingido. Aguarde alguns instantes e tente novamente."
                else ->
                    "⚠️ Falha de comunicação com o Sommelier IA: $msg"
            }
            withContext(Dispatchers.Main) { onToken("\n$userFriendlyError") }
        }
    }

    @Serializable
    private data class OracleAiResponse(
        val perfumeId: String = "",
        val reasoning: String = ""
    )

    suspend fun consultOracleAI(
        place: String,
        placeCustomDetails: String,
        temperature: String,
        temperatureCustomDetails: String,
        perfumes: List<com.aistudio.perfumatico.data.local.PerfumeEntity>
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (perfumes.isEmpty()) return@withContext null

        val perfumeSummaries = perfumes.take(30).joinToString("\n") { p ->
            "- ID: ${p.id} | Nome: ${p.name} | Marca: ${p.brand} | Família: ${p.family} | Notas: ${p.notes} | Tags: ${p.tags} | Fixação: ${p.fixation} | Projeção: ${p.projection}"
        }

        val prompt = """
            Você é o Oráculo Olfativo do aplicativo Perfumático, sommelier supremo de perfumaria.
            O usuário precisa da melhor escolha de perfume da sua própria coleção para a situação:
            - LUGAR / OCASIÃO: $place
            - DETALHES DO LUGAR: ${if (placeCustomDetails.isNotBlank()) placeCustomDetails else "Nenhum detalhe extra"}
            - TEMPERATURA / CLIMA: $temperature
            - DETALHES DO CLIMA/SENSAÇÃO: ${if (temperatureCustomDetails.isNotBlank()) temperatureCustomDetails else "Nenhum detalhe extra"}

            COLEÇÃO DISPONÍVEL DO USUÁRIO:
            $perfumeSummaries

            Analise a harmonia entre as notas olfativas e o ambiente/temperatura descritos.
            Escolha o perfume MAIS ADEQUADO da coleção.
            Retorne APENAS um objeto JSON ESTRITO com o seguinte formato:
            {
              "perfumeId": "<ID exato do perfume escolhido da lista acima>",
              "reasoning": "<Explicação elegante e persuasiva de 2 a 3 frases em português sobre por que esta fragrância se adapta perfeitamente ao local e clima descritos>"
            }
            IMPORTANTE: Sem blocos markdown, crases ou texto fora do JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.2f, responseMimeType = "application/json")
        )

        try {
            val response = executeWithFallback(ACTIVE_MODELS) { model ->
                RetrofitClient.service.generateContent(model, apiKey, request)
            }
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleaned = rawText.replace("```json", "").replace("```", "").trim()
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            val jsonStr = if (start != -1 && end != -1 && end > start) cleaned.substring(start, end + 1) else cleaned
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<OracleAiResponse>(jsonStr)
            if (parsed.perfumeId.isNotBlank()) {
                Pair(parsed.perfumeId, parsed.reasoning)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
