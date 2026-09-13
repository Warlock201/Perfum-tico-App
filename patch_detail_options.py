import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "r") as f:
    content = f.read()

old_list = 'listOf("Frasco", "Decant", "Wishlist", "Nenhum").forEach { st ->'
new_list = 'listOf("Já possuo", "Decant", "Quero ter", "Sonhos Distantes", "Nenhum").forEach { st ->'
content = content.replace(old_list, new_list)

old_color = 'selectedContainerColor = if (st == "Frasco") Amber400 else if (st == "Decant") Emerald400 else if (st == "Wishlist") Cyan500 else Slate700'
new_color = 'selectedContainerColor = if (st == "Já possuo") Amber400 else if (st == "Decant") Emerald400 else if (st == "Quero ter") Cyan500 else if (st == "Sonhos Distantes") Violet500 else Slate700'
content = content.replace(old_color, new_color)

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "w") as f:
    f.write(content)
