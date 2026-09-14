import re

with open(".github/workflows/build_apk.yml", "r") as f:
    content = f.read()

# Add a step to generate a dummy debug.keystore because GitHub still needs it for signing, but it will be fixed
dummy_keystore_step = """
    - name: Generate dummy debug keystore
      run: |
        keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
"""

if "Generate dummy debug keystore" not in content:
    content = content.replace("    - name: Build Debug APK", dummy_keystore_step.lstrip("\n") + "    - name: Build Debug APK")

with open(".github/workflows/build_apk.yml", "w") as f:
    f.write(content)

print("Restored dummy keystore for GitHub Action")
