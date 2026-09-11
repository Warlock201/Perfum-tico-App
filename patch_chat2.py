import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt', 'r') as f:
    code = f.read()

# the previous patch might have been wrong, let's just use imePadding() but apply it correctly
# The issue is that the Scaffold in MainActivity uses contentWindowInsets = WindowInsets.safeDrawing,
# which already applies padding for the top bar and bottom bar, BUT we are rendering ChatScreen inside it.
# We shouldn't need statusBars padding if Scaffold is doing it, but if it's cutting off, maybe we do.

old_column = '''    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {'''

new_column = '''    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .imePadding()
    ) {'''

code = code.replace(old_column, new_column)

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt', 'w') as f:
    f.write(code)
