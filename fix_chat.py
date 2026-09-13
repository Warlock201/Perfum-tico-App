with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
i = 0
while i < len(lines):
    if 'val combinedText =' in lines[i]:
        new_lines.append('                        val combinedText = (last.parts.firstOrNull()?.text ?: "") + "\\n" + (msg.parts.firstOrNull()?.text ?: "")\n')
        i += 2
    else:
        new_lines.append(lines[i])
        i += 1

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.writelines(new_lines)
