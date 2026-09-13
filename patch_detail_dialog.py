import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "r") as f:
    content = f.read()

old_status_logic = """
    var status by remember { mutableStateOf(
        when (perfume.status) {
            "Já possuo", "Frasco" -> "Frasco"
            "Decant" -> "Decant"
            "Pipeline", "Desejos", "Wishlist" -> "Wishlist"
            else -> "Nenhum"
        }
    ) }
"""

new_status_logic = """
    var status by remember { mutableStateOf(
        when (perfume.status) {
            "Já possuo", "Frasco" -> "Já possuo"
            "Decant" -> "Decant"
            "Quero ter", "Pipeline" -> "Quero ter"
            "Sonhos Distantes", "Desejos", "Wishlist" -> "Sonhos Distantes"
            else -> "Nenhum"
        }
    ) }
"""
content = content.replace(old_status_logic.strip(), new_status_logic.strip())

# The choices available in the dropdown
old_options = 'val statusOptions = listOf("Frasco", "Decant", "Wishlist", "Nenhum")'
new_options = 'val statusOptions = listOf("Já possuo", "Decant", "Quero ter", "Sonhos Distantes", "Nenhum")'
content = content.replace(old_options, new_options)

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "w") as f:
    f.write(content)
