import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

# Make sure we actually throw if it fails in streaming so it can fallback
# Oh wait, streamGenerateContent returns a ResponseBody. If it's a 400 or 404, Retrofit THROWS an HttpException which will trigger the fallback. 
# BUT wait. Retrofit won't throw if it's not a successful response but it still returns the ResponseBody? 
# No, Retrofit throws HttpException if responseCode is not 2xx.
# Let's ensure executeWithFallback throws an exception that tells us WHAT the error was so we can debug.

new_fallback = """
    private suspend fun <T> executeWithFallback(
        models: List<String>,
        action: suspend (String) -> T
    ): T {
        var lastError: Exception? = null
        for (model in models) {
            try {
                return action(model)
            } catch (e: retrofit2.HttpException) {
                lastError = e
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                android.util.Log.e("GeminiService", "Erro no modelo $model: HTTP ${e.code()} - $errorBody")
                // Continue to the next model
            } catch (e: Exception) {
                lastError = e
                android.util.Log.e("GeminiService", "Erro genérico no modelo $model: ${e.message}")
            }
        }
        throw Exception("Falha em todos os modelos. Último erro: ${lastError?.message}")
    }
"""

content = re.sub(r'private suspend fun <T> executeWithFallback.*?throw lastError \?: Exception\("All models failed"\)\n    }', new_fallback.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)
