#!/bin/bash
sed -i 's/"gemini-1.5-pro-latest"/"gemini-1.5-pro"/g' app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt
sed -i 's/"gemini-1.5-flash-latest"/"gemini-1.5-flash"/g' app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt
