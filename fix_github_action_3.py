import re

with open(".github/workflows/build_apk.yml", "r") as f:
    content = f.read()

# Add a step to generate a dummy debug.keystore so GitHub Actions doesn't fail signing
dummy_keystore_step = """
    - name: Generate dummy debug keystore
      run: |
        keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
"""

content = content.replace("    - name: Build Debug APK", dummy_keystore_step.lstrip("\n") + "    - name: Build Debug APK")

with open(".github/workflows/build_apk.yml", "w") as f:
    f.write(content)

print("Fixed GitHub Action debug.keystore generation")
