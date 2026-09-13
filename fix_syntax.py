import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if 'val combinedText =' in line and '+"' not in line:
        line = line.replace(') + "\\n" + (', ') + "\\n" + (')
    new_lines.append(line)

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.writelines(new_lines)

