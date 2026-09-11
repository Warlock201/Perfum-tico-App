import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt', 'r') as f:
    code = f.read()

old_column = '''    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .imePadding()
    ) {'''

new_column = '''    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .imePadding()
            .padding(16.dp)
    ) {'''

code = code.replace(old_column, new_column)

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt', 'w') as f:
    f.write(code)
