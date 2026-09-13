import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/PerfumeViewModel.kt", "r") as f:
    content = f.read()

old_enum = """
enum class CollectionSubTab(val dbStatus: String) {
    HAVE("Frasco"),
    DECANT("Decant"),
    WISH("Wishlist")
}
"""

new_enum = """
enum class CollectionSubTab(val dbStatus: String) {
    HAVE("Frasco"),
    DECANT("Decant"),
    WANT("Quero ter"),
    DREAM("Sonhos Distantes")
}
"""

content = content.replace(old_enum.strip(), new_enum.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/PerfumeViewModel.kt", "w") as f:
    f.write(content)
