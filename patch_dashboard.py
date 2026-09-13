import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_lists = """
    val haveList = perfumes.filter { it.status == "Já possuo" }
    val toBuyList = perfumes.filter { it.status == "Pipeline" }
    val wishList = perfumes.filter { it.status == "Desejos" }
"""

new_lists = """
    val haveList = perfumes.filter { it.status == "Já possuo" || it.status == "Frasco" }
    val toBuyList = perfumes.filter { it.status == "Quero ter" || it.status == "Pipeline" }
    val wishList = perfumes.filter { it.status == "Sonhos Distantes" || it.status == "Desejos" || it.status == "Wishlist" }
"""

content = content.replace(old_lists.strip(), new_lists.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
