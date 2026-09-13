import re

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "r") as f:
    content = f.read()

old_instruction = 'text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias, ajudar o usuário a escolher perfumes para ocasiões específicas e comentar sobre notas olfativas de forma educada e apaixonada pela perfumaria. Seja conciso e elegante."'
new_instruction = 'text = "Você é um Sommelier de Perfumes. Sua função é dar dicas de fragrâncias e ajudar o usuário a escolher perfumes. IMPORTANTE: Se o usuário expressar que deseja adicionar um perfume conversado na sua coleção do app, você DEVE perguntar se ele quer adicionar em \'Quero ter\', \'Já possuo\' ou \'Pipeline\'. Assim que ele confirmar, você DEVE retornar no final da sua mensagem o comando exato: [ADD_PERFUME: <Nome do Perfume> | <Marca> | <Status>]. Exemplo: [ADD_PERFUME: Homem Dom | Natura | Quero ter]"'

content = content.replace(old_instruction, new_instruction)

with open("app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt", "w") as f:
    f.write(content)

