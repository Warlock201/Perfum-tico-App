import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'r') as f:
    code = f.read()

# Add states for fixation and projection
state_old = 'var isAutoFilling by remember { mutableStateOf(false) }'
state_new = 'var isAutoFilling by remember { mutableStateOf(false) }\n    var fixation by remember { mutableStateOf(perfumeToEdit?.fixation ?: 7) }\n    var projection by remember { mutableStateOf(perfumeToEdit?.projection ?: 7) }'
code = code.replace(state_old, state_new)

# Re-add JSON parsing
code = code.replace('if (json.has("baseNotes")) baseNotes = json.getString("baseNotes")', 'if (json.has("baseNotes")) baseNotes = json.getString("baseNotes")\n                                            if (json.has("fixation")) fixation = json.getInt("fixation")\n                                            if (json.has("projection")) projection = json.getInt("projection")')

# Inject into PerfumeEntity constructor
# Replace "baseNotes = baseNotes.trim()," with "baseNotes = baseNotes.trim(),\nfixation = fixation,\nprojection = projection,"
# Need to be careful because there are two constructors (one for update, one for insert)

code = code.replace('baseNotes = baseNotes.trim(),', 'baseNotes = baseNotes.trim(),\n                                fixation = fixation,\n                                projection = projection,')


with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'w') as f:
    f.write(code)
