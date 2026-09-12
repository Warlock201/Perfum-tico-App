#!/bin/bash
sed -i 's/import androidx.compose.foundation.layout.*/import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.layout.imePadding/g' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
sed -i 's/modifier = Modifier/modifier = Modifier.imePadding()/g' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
