#!/bin/bash
sed -i 's/"gemini-3.1-pro-preview"/"gemini-1.5-pro-latest"/g' app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt
sed -i 's/"gemini-3.5-flash"/"gemini-1.5-flash-latest"/g' app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt
