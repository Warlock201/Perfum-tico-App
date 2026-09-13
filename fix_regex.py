import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

old_regex = 'val commandRegex = Regex("\\\\[ADD_PERFUME:\\\\s*(.+?)\\\\s*\\\\|\\\\s*(.+?)\\\\s*\\\\|\\\\s*(.+?)\\\\s*\\\\]")'
new_regex = 'val commandRegex = Regex("""\\[ADD_PERFUME:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]""")'

# since there's an error, let's just find the regex line and replace it
lines = content.split('\n')
for i, line in enumerate(lines):
    if "val commandRegex = Regex(" in line:
        lines[i] = '                val commandRegex = Regex("""\\[ADD_PERFUME:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]""")'

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write('\n'.join(lines))

