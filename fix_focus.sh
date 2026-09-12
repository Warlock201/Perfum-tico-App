#!/bin/bash
sed -i 's/import androidx.compose.ui.Modifier/import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalFocusManager/g' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
sed -i 's/val listState = rememberLazyListState()/val listState = rememberLazyListState()\n    val focusManager = LocalFocusManager.current\n    LaunchedEffect(Unit) {\n        focusManager.clearFocus()\n    }/g' app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt
