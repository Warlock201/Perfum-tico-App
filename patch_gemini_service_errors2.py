import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

# Make sure we actually throw on 4xx/5xx in streamGenerateContent
# Since Retrofit's streamGenerateContent returns ResponseBody directly and uses @Streaming,
# if the response code is 400, it MIGHT still return a ResponseBody or throw HttpException.
# BUT we can also just use the regular Call or Response object to inspect the code.
# The safest way is to change the retrofit definition to return Response<ResponseBody> so we can check code.

new_retrofit = """
    @POST("v1beta/models/{model}:streamGenerateContent")
    @Streaming
    suspend fun streamGenerateContent(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Query("alt") alt: String = "sse",
        @Body request: GenerateContentRequest
    ): retrofit2.Response<ResponseBody>
}
"""

content = re.sub(r'@POST\("v1beta/models/\{model\}:streamGenerateContent"\)\n.*?suspend fun streamGenerateContent\(.*?\): ResponseBody\n\}', new_retrofit.strip() + "\n}", content, flags=re.DOTALL)

# And then in the usage:
new_usage = """
        try {
            val response = executeWithFallback(listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.5-flash", "gemini-2.5-pro")) { model -> 
                val res = RetrofitClient.service.streamGenerateContent(model, apiKey, request = request) 
                if (!res.isSuccessful) {
                    val errorBody = res.errorBody()?.string() ?: "Unknown error"
                    throw Exception("HTTP ${res.code()} - $errorBody")
                }
                res.body()!!
            }
            response.source().use { source ->
"""

content = re.sub(r'try \{\n.*?val responseBody = executeWithFallback.*?request = request\) \}\n.*?responseBody\.source\(\)\.use \{ source ->', new_usage.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)
