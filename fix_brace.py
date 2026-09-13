with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

content = content.replace("): retrofit2.Response<ResponseBody>\n}\n}", "): retrofit2.Response<ResponseBody>\n}")

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)
