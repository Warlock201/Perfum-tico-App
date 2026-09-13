import re

with open("app/src/main/java/com/aistudio/perfumatico/data/model/Models.kt", "r") as f:
    content = f.read()

old_vols = """val AVAILABLE_VOLUMES = listOf(
    "200ml",
    "150ml",
    "125ml",
    "100ml",
    "90ml",
    "75ml",
    "50ml",
    "30ml",
    "15ml",
    "Decant 10ml",
    "Decant 5ml",
    "Amostra",
    "DUPE"
)"""

new_vols = """val AVAILABLE_VOLUMES = listOf(
    "200ml",
    "150ml",
    "125ml",
    "100ml",
    "90ml",
    "75ml",
    "50ml",
    "30ml",
    "15ml",
    "Decant 20ml",
    "Decant 10ml",
    "Decant 5ml",
    "Amostra"
)"""
content = content.replace(old_vols, new_vols)

with open("app/src/main/java/com/aistudio/perfumatico/data/model/Models.kt", "w") as f:
    f.write(content)
