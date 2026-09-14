import re

with open(".github/workflows/build_apk.yml", "r") as f:
    content = f.read()

# Remove the dummy keystore generation
dummy_keystore_step = """
    - name: Generate dummy debug keystore
      run: |
        keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
"""

content = content.replace(dummy_keystore_step.lstrip("\n"), "")

with open(".github/workflows/build_apk.yml", "w") as f:
    f.write(content)

print("Removed random keystore generation")
