import re

# Update build.gradle.kts
with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = re.sub(r'versionCode = \d+', 'versionCode = 8', content)
content = re.sub(r'versionName = "[^"]+"', 'versionName = "1.7"', content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)

# Update update.json
import json
with open("releases/update.json", "r") as f:
    update_data = json.load(f)

update_data["versionCode"] = 8
update_data["versionName"] = "1.7"
update_data["apkUrl"] = "https://raw.githubusercontent.com/Warlock201/Perfum-tico-App/main/releases/perfumatico-v1.7.apk"
update_data["releaseNotes"] = "Versão 1.7: Correção na configuração de assinatura."

with open("releases/update.json", "w") as f:
    json.dump(update_data, f, indent=2, ensure_ascii=False)

print("Versions updated to 1.7")
