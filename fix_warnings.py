import re

# 1. AppDatabase.kt
with open("app/src/main/java/com/aistudio/perfumatico/data/local/AppDatabase.kt", "r") as f:
    content = f.read()
content = content.replace("fallbackToDestructiveMigration()", "fallbackToDestructiveMigration(dropAllTables = true)")
with open("app/src/main/java/com/aistudio/perfumatico/data/local/AppDatabase.kt", "w") as f:
    f.write(content)

# 2. DashboardScreen.kt
with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()
content = content.replace('Locale("pt", "BR")', 'java.util.Locale.forLanguageTag("pt-BR")')
content = content.replace("Icons.Filled.TrendingUp", "Icons.AutoMirrored.Filled.TrendingUp")
if "import androidx.compose.material.icons.automirrored.filled.TrendingUp" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.TrendingUp", "import androidx.compose.material.icons.automirrored.filled.TrendingUp")
with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

# 3. ProfileSotdDialog.kt
with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ProfileSotdDialog.kt", "r") as f:
    content = f.read()
content = content.replace("Icons.Filled.Logout", "Icons.AutoMirrored.Filled.Logout")
if "import androidx.compose.material.icons.automirrored.filled.Logout" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Logout", "import androidx.compose.material.icons.automirrored.filled.Logout")

# Add @file:Suppress("DEPRECATION") to the top of ProfileSotdDialog.kt to handle GoogleSignIn warnings
if '@file:Suppress("DEPRECATION")' not in content:
    content = '@file:Suppress("DEPRECATION")\n' + content

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ProfileSotdDialog.kt", "w") as f:
    f.write(content)

# 4. Theme.kt
with open("app/src/main/java/com/aistudio/perfumatico/ui/theme/Theme.kt", "r") as f:
    content = f.read()
# Add @Suppress("DEPRECATION") before the View/Window modification block
content = content.replace("val window = (view.context as Activity).window", "@Suppress(\"DEPRECATION\")\n            val window = (view.context as Activity).window")
with open("app/src/main/java/com/aistudio/perfumatico/ui/theme/Theme.kt", "w") as f:
    f.write(content)

print("Warnings fixed")
