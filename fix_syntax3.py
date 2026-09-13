with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'val combinedText =' in line:
        lines[i] = '                        val combinedText = (last.parts.firstOrNull()?.text ?: "") + "\\n" + text\n'

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.writelines(lines)

