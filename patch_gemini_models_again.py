import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

# Models are correct, but just to be 100% sure we don't hit model mismatch, let's use gemini-1.5-flash as the FIRST one.
new_models = 'listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.5-flash", "gemini-2.5-pro")'
content = re.sub(r'listOf\("gemini-2\.5-pro", "gemini-2\.5-flash", "gemini-1\.5-flash", "gemini-1\.5-pro"\)', new_models, content)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)

