import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt', 'r') as f:
    code = f.read()

# Make it use WindowInsets padding properly. 
# `MainActivity.kt`'s Scaffold gives `innerPadding`. The `ChatScreen` isn't receiving `innerPadding`.
# Ah! In MainActivity: 
# MainTab.CHATBOT -> ChatScreen(viewModel = viewModel)
# It's inside a Box that has `modifier = Modifier.fillMaxSize().padding(innerPadding)`
# So ChatScreen ALREADY has the top and bottom padding from the Scaffold!
# But the keyboard padding (imePadding) is causing issues when combined with innerPadding.
# We should remove `.padding(16.dp)` from the Column because we want the keyboard to push the content up,
# and we add `.imePadding()` to the Column.

old_column = '''    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .imePadding()
            .padding(16.dp)
    ) {'''

new_column = '''    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
            .imePadding()
    ) {'''

code = code.replace(old_column, new_column)

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt', 'w') as f:
    f.write(code)
