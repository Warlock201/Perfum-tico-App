import re

# Fix ChatScreen.kt
with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

lines = content.split('\n')
for i, line in enumerate(lines):
    if "val askRegex = Regex(" in line:
        lines[i] = '                val askRegex = Regex("""\\[ASK_COLLECTION:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]""")'

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt", "w") as f:
    f.write('\n'.join(lines))


# Fix ChatViewModel.kt
with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

lines = content.split('\n')
for i, line in enumerate(lines):
    if "val cleanText = text.replace(Regex(" in line:
        lines[i] = '            val cleanText = text.replace(Regex("""\\[ASK_COLLECTION:.*?\\]"""), "").trim()'

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write('\n'.join(lines))
