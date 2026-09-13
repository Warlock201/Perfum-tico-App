import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

old_sys = """
        val systemInstruction = Content(
            parts = listOf(Part(text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção, você NÃO precisa perguntar em qual categoria, pois botões aparecerão na tela. Você DEVE APENAS retornar no final da sua mensagem o comando exato: [ASK_COLLECTION: <Nome do Perfume> | <Marca>]. Exemplo: [ASK_COLLECTION: Homem Dom | Natura]" )),
            role = "system"
        )
"""
new_sys = """
        val systemInstruction = Content(
            parts = listOf(Part(text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção, você NÃO precisa perguntar em qual categoria, pois botões aparecerão na tela. Você DEVE APENAS retornar no final da sua mensagem o comando exato: [ADD_PERFUME: <Nome do Perfume> | <Marca> | <Status>]. Exemplo: [ADD_PERFUME: Homem Dom | Natura | Quero ter]" ))
        )
"""
content = content.replace(old_sys.strip(), new_sys.strip())

# Also update the list of models just to be safe
old_models = 'listOf("gemini-3.1-pro-preview", "gemini-3.5-flash", "gemini-3.1-flash-lite")'
new_models = 'listOf("gemini-2.5-flash", "gemini-1.5-flash")' # Actually we should use gemini-1.5-flash or gemini-2.5-flash if 3.x is failing. But instructions say gemini-3.5-flash.
# Wait, gemini-3.5-flash is in the instruction. Let's keep the models list but ensure history is valid.
# Let's check how history is passed.

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)
