import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

# Replace all fallback lists with the EXACT models from the original instructions
content = re.sub(
    r'listOf\("gemini-1\.5-flash", "gemini-1\.5-flash-latest", "gemini-2\.0-flash-exp"\)',
    'listOf("gemini-3.5-flash", "gemini-3.1-flash-lite")',
    content
)

content = re.sub(
    r'listOf\("gemini-1\.5-pro", "gemini-1\.5-pro-latest", "gemini-2\.0-pro-exp", "gemini-1\.5-flash"\)',
    'listOf("gemini-3.1-pro-preview", "gemini-3.5-flash", "gemini-3.1-flash-lite")',
    content
)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)

