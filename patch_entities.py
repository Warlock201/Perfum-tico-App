import re

with open("app/src/main/java/com/aistudio/perfumatico/data/local/Entities.kt", "r") as f:
    content = f.read()

# Add markedNewAt
old_fields = """
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
"""

new_fields = """
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val markedNewAt: Long = 0L // 0 means not marked as new
)
"""
content = content.replace(old_fields.strip(), new_fields.strip())

with open("app/src/main/java/com/aistudio/perfumatico/data/local/Entities.kt", "w") as f:
    f.write(content)
