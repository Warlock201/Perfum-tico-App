package com.aistudio.perfumatico.data.local

import android.content.Context
import android.content.SharedPreferences
import com.aistudio.perfumatico.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiKeyManager {
    private const val PREFS_NAME = "perfumatico_api_keys"
    private const val KEY_CUSTOM_GEMINI = "custom_gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCustomGeminiApiKey(context: Context): String? {
        val key = getPrefs(context).getString(KEY_CUSTOM_GEMINI, null)?.trim()
        return if (key.isNullOrBlank()) null else key
    }

    fun setCustomGeminiApiKey(context: Context, key: String?) {
        val trimmed = key?.trim()
        if (trimmed.isNullOrBlank()) {
            getPrefs(context).edit().remove(KEY_CUSTOM_GEMINI).apply()
        } else {
            getPrefs(context).edit().putString(KEY_CUSTOM_GEMINI, trimmed).apply()
        }
    }

    fun getEffectiveApiKey(context: Context?): String {
        if (context != null) {
            val custom = getCustomGeminiApiKey(context)
            if (!custom.isNullOrBlank()) {
                return custom
            }
        }
        val envNew = BuildConfig.GEMINI_API_KEY_NEW.trim()
        if (envNew.isNotBlank()) return envNew
        return BuildConfig.GEMINI_API_KEY.trim()
    }

    fun isKeyFormatLikelyValid(key: String): Boolean {
        val trimmed = key.trim()
        return trimmed.length >= 30
    }

    suspend fun testApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(Exception("A chave de API não pode estar vazia."))
        }
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val jsonBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "ping" }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$trimmed")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success("Chave válida! Conexão com Gemini estabelecida com sucesso.")
            } else {
                val errorMsg = when {
                    body.contains("API_KEY_INVALID") -> "Chave inválida (API_KEY_INVALID). Certifique-se de usar uma chave do Google AI Studio."
                    body.contains("API_KEY_SERVICE_BLOCKED") -> "Chave bloqueada para Generative Language API (API_KEY_SERVICE_BLOCKED). Habilite o serviço no console ou use uma chave gerada no Google AI Studio."
                    body.contains("PERMISSION_DENIED") -> "Permissão negada (PERMISSION_DENIED). Verifique as restrições da chave."
                    body.contains("RESOURCE_EXHAUSTED") -> "Cota atingida (RESOURCE_EXHAUSTED). Tente novamente em instantes."
                    body.contains("models/gemini-3.8-flash is not found") || body.contains("NOT_FOUND") -> {
                        // Fallback test
                        Result.success("Chave parece válida (respondeu NOT_FOUND para o modelo, mas não recusou a chave).")
                        return@withContext Result.success("Chave provavelmente válida (modelo indisponível, mas chave aceita).")
                    }
                    else -> "Erro HTTP ${response.code}: $body"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha na conexão: ${e.message}"))
        }
    }
}
