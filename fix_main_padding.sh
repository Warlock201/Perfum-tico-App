#!/bin/bash
sed -i 's/\.padding(innerPadding)/.padding(innerPadding).imePadding()/g' app/src/main/java/com/aistudio/perfumatico/MainActivity.kt
