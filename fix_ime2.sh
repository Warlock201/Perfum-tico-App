#!/bin/bash
git checkout app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
sed -i 's/modifier = Modifier/modifier = Modifier.imePadding()/1' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
