import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/MyPerfumesScreen.kt", "r") as f:
    content = f.read()

old_filter = """
            val matchesTab = p.status == currentSubTab.dbStatus || 
                (currentSubTab == CollectionSubTab.HAVE && (p.status == "Já possuo" || p.status == "Frasco")) ||
                (currentSubTab == CollectionSubTab.DECANT && p.status == "Decant") ||
                (currentSubTab == CollectionSubTab.WANT && (p.status == "Quero ter" || p.status == "Pipeline")) ||
                (currentSubTab == CollectionSubTab.DREAM && (p.status == "Graal" || p.status == "Desejos" || p.status == "Wishlist"))
"""

new_filter = """
            val matchesTab = p.status == currentSubTab.dbStatus || 
                (currentSubTab == CollectionSubTab.HAVE && (p.status == "Já possuo" || p.status == "Frasco" || p.status == "Decant")) ||
                (currentSubTab == CollectionSubTab.WANT && (p.status == "Quero ter" || p.status == "Pipeline")) ||
                (currentSubTab == CollectionSubTab.DREAM && (p.status == "Graal" || p.status == "Desejos" || p.status == "Wishlist"))
"""
content = content.replace(old_filter.strip(), new_filter.strip())

old_tabs = """
            val tabs = listOf(
                CollectionSubTab.HAVE to "FRASCOS",
                CollectionSubTab.DECANT to "DECANTS",
                CollectionSubTab.WANT to "QUERO TER",
                CollectionSubTab.DREAM to "GRAAL"
            )
"""

new_tabs = """
            val tabs = listOf(
                CollectionSubTab.HAVE to "COLEÇÃO",
                CollectionSubTab.WANT to "QUERO TER",
                CollectionSubTab.DREAM to "GRAAL"
            )
"""
content = content.replace(old_tabs.strip(), new_tabs.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/MyPerfumesScreen.kt", "w") as f:
    f.write(content)
