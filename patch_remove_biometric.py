import re

with open('gradle/libs.versions.toml', 'r') as f:
    text = f.read()
text = text.replace('biometric = "1.1.0"\n', '')
text = text.replace('androidx-biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }\n', '')
with open('gradle/libs.versions.toml', 'w') as f:
    f.write(text)

with open('app/build.gradle.kts', 'r') as f:
    text = f.read()
text = text.replace('    implementation(libs.androidx.biometric)\n', '')
with open('app/build.gradle.kts', 'w') as f:
    f.write(text)
