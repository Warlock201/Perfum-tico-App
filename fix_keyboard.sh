#!/bin/bash
sed -i 's/\.padding(innerPadding)\.imePadding()/.padding(innerPadding)/g' app/src/main/java/com/aistudio/perfumatico/MainActivity.kt
sed -i 's/modifier = Modifier/modifier = Modifier.imePadding()/1' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
sed -i 's/modifier = Modifier/modifier = Modifier.imePadding()/1' app/src/main/java/com/aistudio/perfumatico/ui/screens/CatalogScreen.kt
