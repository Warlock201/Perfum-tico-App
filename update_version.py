import re

# Update build.gradle.kts
with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = re.sub(r'versionCode = \d+', 'versionCode = 6', content)
content = re.sub(r'versionName = "[^"]+"', 'versionName = "1.5"', content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)

# Update update.json
import json
with open("releases/update.json", "r") as f:
    update_data = json.load(f)

update_data["versionCode"] = 6
update_data["versionName"] = "1.5"
update_data["apkUrl"] = "https://raw.githubusercontent.com/Warlock201/Perfum-tico-App/main/releases/perfumatico-v1.5.apk"
update_data["releaseNotes"] = "Versão 1.5: Fix da API do Gemini (correção de histórico e chave de acesso) e resolução de warnings do sistema."

with open("releases/update.json", "w") as f:
    json.dump(update_data, f, indent=2, ensure_ascii=False)

print("Versions updated")
