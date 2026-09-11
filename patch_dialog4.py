import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'r') as f:
    code = f.read()

code = code.replace('fixation = 7,\n                                projection = 7,', '')

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'w') as f:
    f.write(code)
