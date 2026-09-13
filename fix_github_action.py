with open(".github/workflows/build_apk.yml", "r") as f:
    content = f.read()

content = content.replace('echo "GEMINI_API_KEY=dummy_key" >> .env', 'echo "GEMINI_API_KEY=dummy_key" >> .env\n        echo "GEMINI_API_KEY_NEW=dummy_key" >> .env')

with open(".github/workflows/build_apk.yml", "w") as f:
    f.write(content)

print("Fixed GitHub Action .env generation")
