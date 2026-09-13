import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/PerfumeViewModel.kt", "r") as f:
    content = f.read()

old_enum = """
enum class MainTab {
    DASHBOARD, COLLECTION, DISCOVER, CATALOG, CHATBOT
}
"""
new_enum = """
enum class MainTab {
    DASHBOARD, COLLECTION, DISCOVER, CATALOG, CHATBOT, ORACLE
}
"""
if old_enum.strip() in content:
    content = content.replace(old_enum.strip(), new_enum.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/PerfumeViewModel.kt", "w") as f:
    f.write(content)
