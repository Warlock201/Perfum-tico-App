import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('""") + "\n" + (msg', '""") + "\\n" + (msg')

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write(content)
