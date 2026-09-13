import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

old_instruction = r'text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção do app, você DEVE perguntar se ele quer adicionar em \'Quero ter\', \'Já possuo\' ou \'Pipeline\'. Assim que ele confirmar, você DEVE retornar no final da sua mensagem o comando exato: [ADD_PERFUME: <Nome do Perfume> | <Marca> | <Status>]. Exemplo: [ADD_PERFUME: Homem Dom | Natura | Quero ter]"'
new_instruction = r'text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção, você NÃO precisa perguntar em qual categoria, pois botões aparecerão na tela. Você DEVE APENAS retornar no final da sua mensagem o comando exato: [ASK_COLLECTION: <Nome do Perfume> | <Marca>]. Exemplo: [ASK_COLLECTION: Homem Dom | Natura]"'

content = content.replace('text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção do app, você DEVE perguntar se ele quer adicionar em \'Quero ter\', \'Já possuo\' ou \'Pipeline\'. Assim que ele confirmar, você DEVE retornar no final da sua mensagem o comando exato: [ADD_PERFUME: <Nome do Perfume> | <Marca> | <Status>]. Exemplo: [ADD_PERFUME: Homem Dom | Natura | Quero ter]"', 'text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção, você NÃO precisa perguntar em qual categoria, pois botões aparecerão na tela. Você DEVE APENAS retornar no final da sua mensagem o comando exato: [ASK_COLLECTION: <Nome do Perfume> | <Marca>]. Exemplo: [ASK_COLLECTION: Homem Dom | Natura]"')

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)
