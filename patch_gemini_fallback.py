import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

# Models fallbacks to use
FLASH_MODELS = 'listOf("gemini-1.5-flash", "gemini-1.5-flash-latest", "gemini-2.0-flash-exp")'
PRO_MODELS = 'listOf("gemini-1.5-pro", "gemini-1.5-pro-latest", "gemini-2.0-pro-exp", "gemini-1.5-flash")'

# Inject executeWithFallback helper
fallback_helper = """
    private suspend fun <T> executeWithFallback(
        models: List<String>,
        action: suspend (String) -> T
    ): T {
        var lastError: Exception? = null
        for (model in models) {
            try {
                return action(model)
            } catch (e: Exception) {
                lastError = e
                // Continue to the next model
            }
        }
        throw lastError ?: Exception("All models failed")
    }
"""
content = content.replace("object GeminiService {", f"object GeminiService {{{fallback_helper}")

# Fix autoFillPerfume
content = re.sub(
    r'val response = RetrofitClient\.service\.generateContent\("gemini-3\.5-flash", apiKey, request\)',
    f'val response = executeWithFallback({FLASH_MODELS}) {{ model -> RetrofitClient.service.generateContent(model, apiKey, request) }}',
    content
)

# Fix completeOlfactoryPyramid
content = re.sub(
    r'val response = RetrofitClient\.service\.generateContent\("gemini-3\.5-flash", apiKey, request\)',
    f'val response = executeWithFallback({FLASH_MODELS}) {{ model -> RetrofitClient.service.generateContent(model, apiKey, request) }}',
    content
)

# Fix chatWithSommelier
content = re.sub(
    r'val responseBody = RetrofitClient\.service\.streamGenerateContent\("gemini-3\.1-pro-preview", apiKey, request = request\)',
    f'val responseBody = executeWithFallback({PRO_MODELS}) {{ model -> RetrofitClient.service.streamGenerateContent(model, apiKey, request = request) }}',
    content
)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)

