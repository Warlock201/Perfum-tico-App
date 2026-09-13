#!/bin/bash
sed -i 's/"gemini-1.5-pro"/"gemini-3.1-pro-preview"/g' app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt
sed -i 's/"gemini-1.5-flash"/"gemini-3.5-flash"/g' app/src/main/java/com/aistudio/perfumatico/data/remote/GeminiService.kt
