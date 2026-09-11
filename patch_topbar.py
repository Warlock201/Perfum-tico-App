import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt', 'r') as f:
    code = f.read()

old_row = '''        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),'''

new_row = '''        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),'''

code = code.replace(old_row, new_row)

with open('app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt', 'w') as f:
    f.write(code)
