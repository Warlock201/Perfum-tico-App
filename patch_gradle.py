import re

with open('app/build.gradle.kts', 'r') as f:
    text = f.read()

text = text.replace('dependencies {\n', 'dependencies {\n    implementation(libs.androidx.biometric)\n')
text = text.replace('isMinifyEnabled = false', 'isMinifyEnabled = true\n            isShrinkResources = true')

with open('app/build.gradle.kts', 'w') as f:
    f.write(text)
