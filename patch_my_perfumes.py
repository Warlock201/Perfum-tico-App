import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/MyPerfumesScreen.kt", "r") as f:
    content = f.read()

# Replace the filtering logic
old_filter = """
            val matchesTab = p.status == currentSubTab.dbStatus || 
                (currentSubTab == CollectionSubTab.HAVE && (p.status == "Já possuo" || p.status == "Frasco")) ||
                (currentSubTab == CollectionSubTab.DECANT && p.status == "Decant") ||
                (currentSubTab == CollectionSubTab.WISH && (p.status == "Desejos" || p.status == "Pipeline" || p.status == "Wishlist"))
"""

new_filter = """
            val matchesTab = p.status == currentSubTab.dbStatus || 
                (currentSubTab == CollectionSubTab.HAVE && (p.status == "Já possuo" || p.status == "Frasco")) ||
                (currentSubTab == CollectionSubTab.DECANT && p.status == "Decant") ||
                (currentSubTab == CollectionSubTab.WANT && (p.status == "Quero ter" || p.status == "Pipeline")) ||
                (currentSubTab == CollectionSubTab.DREAM && (p.status == "Sonhos Distantes" || p.status == "Desejos" || p.status == "Wishlist"))
"""

content = content.replace(old_filter.strip(), new_filter.strip())

# Replace the tab buttons definition
old_tabs = """
            val tabs = listOf(
                CollectionSubTab.HAVE to "FRASCOS",
                CollectionSubTab.DECANT to "DECANTS",
                CollectionSubTab.WISH to "WISHLIST"
            )
"""

new_tabs = """
            val tabs = listOf(
                CollectionSubTab.HAVE to "FRASCOS",
                CollectionSubTab.DECANT to "DECANTS",
                CollectionSubTab.WANT to "QUERO TER",
                CollectionSubTab.DREAM to "SONHOS DISTANTES"
            )
"""
content = content.replace(old_tabs.strip(), new_tabs.strip())

# Add horizontal scroll to the Row of buttons if not already there, wait, it's Row(modifier = Modifier...)
# Let's see the line before `val tabs = listOf(`
old_row = """
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(12.dp))
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
"""

new_row = """
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(12.dp))
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .padding(4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
"""
content = content.replace(old_row.strip(), new_row.strip())

# also make sure rememberScrollState and horizontalScroll are imported. (They probably are since it's used below, but I will double check)

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/MyPerfumesScreen.kt", "w") as f:
    f.write(content)
