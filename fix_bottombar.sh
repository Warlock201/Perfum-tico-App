#!/bin/bash
sed -i 's/import androidx.compose.ui.unit.sp/import androidx.compose.ui.unit.sp\nimport androidx.compose.foundation.layout.WindowInsets\nimport androidx.compose.foundation.layout.isImeVisible/g' app/src/main/java/com/aistudio/perfumatico/MainActivity.kt
sed -i 's/bottomBar = {/bottomBar = {\n            val isImeVisible = WindowInsets.isImeVisible\n            if (!isImeVisible) {/g' app/src/main/java/com/aistudio/perfumatico/MainActivity.kt
sed -i 's/onTabSelected = { viewModel.setTab(it) }/onTabSelected = { viewModel.setTab(it) }\n            )\n            }/g' app/src/main/java/com/aistudio/perfumatico/MainActivity.kt
