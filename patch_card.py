import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "r") as f:
    content = f.read()

old_logic = """
                    val typeText = if (perfume.status == "Decant") "Decant" else "Frasco"
                    val volumeText = try {
"""

new_logic = """
                    val volumeText = try {
"""
content = content.replace(old_logic.strip(), new_logic.strip())

old_row = """
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$typeText • $volumeText",
"""

new_row = """
                    val typeText = if (volumeText.contains("Decant", ignoreCase = true) || volumeText.contains("Amostra", ignoreCase = true)) "Decant" else "Frasco"
                    val displayVol = volumeText.replace("Decant ", "", ignoreCase = true)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$typeText • $displayVol",
"""
content = content.replace(old_row.strip(), new_row.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "w") as f:
    f.write(content)
