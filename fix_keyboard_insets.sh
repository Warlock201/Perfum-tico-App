#!/bin/bash
sed -i 's/modifier = Modifier.navigationBarsPadding().imePadding()/modifier = Modifier/g' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
