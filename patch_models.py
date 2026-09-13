import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

new_models = 'listOf("gemini-2.5-pro", "gemini-2.5-flash", "gemini-1.5-flash", "gemini-1.5-pro")'

content = re.sub(r'listOf\("gemini-3\.5-flash", "gemini-3\.1-flash-lite"\)', new_models, content)
content = re.sub(r'listOf\("gemini-3\.1-pro-preview", "gemini-3\.5-flash", "gemini-3\.1-flash-lite"\)', new_models, content)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)
