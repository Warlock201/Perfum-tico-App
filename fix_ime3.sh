#!/bin/bash
sed -i 's/modifier = Modifier.imePadding()/modifier = Modifier/g' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
sed -i '0,/modifier = Modifier/s//modifier = Modifier.imePadding()/' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
