import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'r') as f:
    code = f.read()

# remove fixation and projection parsing
code = code.replace('if (json.has("fixation")) fixation = json.getInt("fixation")', '')
code = code.replace('if (json.has("projection")) projection = json.getInt("projection")', '')

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'w') as f:
    f.write(code)
