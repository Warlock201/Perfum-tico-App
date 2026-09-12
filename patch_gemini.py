import re

with open('app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt', 'r') as f:
    code = f.read()

# Replace hardcoded model in annotations with Path parameter
code = code.replace(
'''    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/gemini-3.5-flash:streamGenerateContent")
    @Streaming
    suspend fun streamGenerateContent(
        @Query("key") apiKey: String,
        @Query("alt") alt: String = "sse",
        @Body request: GenerateContentRequest
    ): ResponseBody''',
'''    @POST("v1beta/models/{model}:generateContent")
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
    ): ResponseBody'''
)

# Fix API calls to pass the model
code = code.replace(
    'val response = RetrofitClient.service.generateContent(apiKey, request)',
    'val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)'
)
code = code.replace(
    'val responseBody = RetrofitClient.service.streamGenerateContent(apiKey, request = request)',
    'val responseBody = RetrofitClient.service.streamGenerateContent("gemini-3.1-pro-preview", apiKey, request = request)'
)

with open('app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt', 'w') as f:
    f.write(code)
