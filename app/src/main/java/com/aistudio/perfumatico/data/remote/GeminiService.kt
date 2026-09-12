package com.aistudio.perfumatico.data.remote

import com.aistudio.perfumatico.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:streamGenerateContent")
    @Streaming
    suspend fun streamGenerateContent(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Query("alt") alt: String = "sse",
        @Body request: GenerateContentRequest
    ): ResponseBody
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
    suspend fun completeOlfactoryPyramid(perfumeName: String, brandName: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
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
            val response = RetrofitClient.service.generateContent("gemini-1.5-flash-latest", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            "Erro ao buscar notas com IA: ${e.message}"
        }
    }

    
    suspend fun autoFillPerfume(perfumeName: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Você é um assistente de banco de dados de perfumaria. O usuário quer adicionar o perfume '$perfumeName'.
            Faça uma pesquisa atualizada na internet para confirmar os detalhes reais desse perfume.
            Retorne um objeto JSON ESTRITO com a seguinte estrutura:
            {
                "brand": "Marca do Perfume",
                "family": "Família Olfativa principal (ex: Cítrico, Amadeirado, Oriental, Floral, etc)",
                "topNotes": "Nota 1, Nota 2",
                "heartNotes": "Nota 3, Nota 4",
                "baseNotes": "Nota 5, Nota 6",
                "fixation": 8, // Inteiro de 1 a 10
                "projection": 7 // Inteiro de 1 a 10
            }
            IMPORTANTE: Não adicione nenhum markdown, backticks (```) ou texto fora do JSON. Apenas o JSON puro.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.1f, responseMimeType = "application/json"),
            tools = listOf(Tool(googleSearch = GoogleSearch()))
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-1.5-flash-latest", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
        } catch (e: Exception) {
            "{}"
        }
    }

    suspend fun chatWithSommelier(history: List<Content>, onToken: (String) -> Unit) = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val systemInstruction = Content(
            parts = listOf(Part(text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias, ajudar o usuário a escolher perfumes para ocasiões específicas e comentar sobre notas olfativas de forma educada e apaixonada pela perfumaria. Seja conciso e elegante." )),
            role = "system"
        )
        
        val request = GenerateContentRequest(
            contents = history,
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val responseBody = RetrofitClient.service.streamGenerateContent("gemini-1.5-pro-latest", apiKey, request = request)
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
            withContext(Dispatchers.Main) { onToken("\n[Erro de conexão: ${e.message}]") }
        }
    }
}
