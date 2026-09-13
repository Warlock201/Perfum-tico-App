import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "r") as f:
    content = f.read()

old_status_logic = """
    var status by remember { mutableStateOf(
        when (perfume.status) {
            "Já possuo", "Frasco" -> "Já possuo"
            "Decant" -> "Decant"
            "Quero ter", "Pipeline" -> "Quero ter"
            "Graal", "Desejos", "Wishlist" -> "Graal"
            else -> "Nenhum"
        }
    ) }
"""

new_status_logic = """
    var status by remember { mutableStateOf(
        when (perfume.status) {
            "Já possuo", "Frasco", "Decant" -> "Já possuo"
            "Quero ter", "Pipeline" -> "Quero ter"
            "Graal", "Desejos", "Wishlist" -> "Graal"
            else -> "Nenhum"
        }
    ) }
"""
content = content.replace(old_status_logic.strip(), new_status_logic.strip())


old_list_forEach = """
                            listOf("Já possuo", "Decant", "Quero ter", "Graal", "Nenhum").forEach { st ->
"""

new_list_forEach = """
                            listOf("Já possuo", "Quero ter", "Graal", "Nenhum").forEach { st ->
"""
content = content.replace(old_list_forEach.strip(), new_list_forEach.strip())

old_colors = """
                                        selectedContainerColor = if (st == "Já possuo") Amber400 else if (st == "Decant") Emerald400 else if (st == "Quero ter") Cyan500 else if (st == "Graal") Purple500 else Slate700,
"""
new_colors = """
                                        selectedContainerColor = if (st == "Já possuo") Amber400 else if (st == "Quero ter") Cyan500 else if (st == "Graal") Purple500 else Slate700,
"""
content = content.replace(old_colors.strip(), new_colors.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "w") as f:
    f.write(content)
