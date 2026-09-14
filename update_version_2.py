import re

# Update build.gradle.kts
with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = re.sub(r'versionCode = \d+', 'versionCode = 7', content)
content = re.sub(r'versionName = "[^"]+"', 'versionName = "1.6"', content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)

# Update update.json
import json
with open("releases/update.json", "r") as f:
    update_data = json.load(f)

update_data["versionCode"] = 7
update_data["versionName"] = "1.6"
update_data["apkUrl"] = "https://raw.githubusercontent.com/Warlock201/Perfum-tico-App/main/releases/perfumatico-v1.6.apk"
update_data["releaseNotes"] = "Versão 1.6: Atualização da assinatura do aplicativo e correção definitiva do Login do Google com Firebase."

with open("releases/update.json", "w") as f:
    json.dump(update_data, f, indent=2, ensure_ascii=False)

print("Versions updated to 1.6")
