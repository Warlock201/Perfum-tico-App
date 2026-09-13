import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Check if play-services-location is present
if "play-services-location" not in content:
    old_deps = """
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
"""
    new_deps = """
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.android.gms:play-services-location:21.0.1")
"""
    content = content.replace(old_deps, new_deps)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
