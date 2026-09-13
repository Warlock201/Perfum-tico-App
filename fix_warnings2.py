import re

# DashboardScreen.kt missing icon replace
with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()
content = content.replace("Icons.Filled.TrendingUp", "Icons.AutoMirrored.Filled.TrendingUp")
if "import androidx.compose.material.icons.automirrored.filled.TrendingUp" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.TrendingUp", "import androidx.compose.material.icons.automirrored.filled.TrendingUp")
with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

# Theme.kt deprecations
with open("app/src/main/java/com/aistudio/perfumatico/ui/theme/Theme.kt", "r") as f:
    content = f.read()

# Make sure Suppress deprecation wraps the whole function or SideEffect to catch everything
if '@Suppress("DEPRECATION")' in content:
    content = content.replace('@Suppress("DEPRECATION")', '')

content = content.replace("    SideEffect {", "    @Suppress(\"DEPRECATION\")\n    SideEffect {")
with open("app/src/main/java/com/aistudio/perfumatico/ui/theme/Theme.kt", "w") as f:
    f.write(content)

print("Remaining warnings fixed")
