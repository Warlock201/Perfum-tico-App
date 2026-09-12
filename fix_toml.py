import re

with open('gradle/libs.versions.toml', 'r') as f:
    text = f.read()

# Remove the appended part
text = text.replace('biometric = "1.1.0"\n[libraries]\nandroidx-biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }\n', '')

# Insert versions
text = text.replace('[versions]\n', '[versions]\nbiometric = "1.1.0"\n')

# Insert libraries
text = text.replace('[libraries]\n', '[libraries]\nandroidx-biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }\n')

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(text)
