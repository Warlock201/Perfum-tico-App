import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "r") as f:
    content = f.read()

old_items = """
        val items = listOf(
            Triple(MainTab.COLLECTION, "Meus", Icons.Default.Diamond),
            Triple(MainTab.DASHBOARD, "Estatísticas", Icons.Default.Analytics),
            Triple(MainTab.DISCOVER, "Descubra", Icons.Default.AutoAwesome),
            Triple(MainTab.CATALOG, "Catálogo", Icons.Default.MenuBook),
            Triple(MainTab.CHATBOT, "Chat", Icons.Default.VoiceChat)
        )
"""

new_items = """
        val items = listOf(
            Triple(MainTab.COLLECTION, "Meus", Icons.Default.Diamond),
            Triple(MainTab.DASHBOARD, "Painel", Icons.Default.Analytics),
            Triple(MainTab.DISCOVER, "Descubra", Icons.Default.Search),
            Triple(MainTab.ORACLE, "Oráculo", Icons.Default.AutoAwesome),
            Triple(MainTab.CHATBOT, "Chat", Icons.Default.VoiceChat)
        )
"""
content = content.replace(old_items.strip(), new_items.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "w") as f:
    f.write(content)
