import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

old_send = """
        viewModelScope.launch {
            val historyToSend = _chatHistory.value.take(assistantMsgIndex)
            val geminiService = GeminiService
"""

new_send = """
        viewModelScope.launch {
            // Clean up history to ensure alternating roles (API requirement)
            val rawHistory = _chatHistory.value.take(assistantMsgIndex)
            val historyToSend = mutableListOf<Content>()
            var expectedRole = "user"
            for (msg in rawHistory) {
                // Skip empty messages or error messages
                if (msg.parts.firstOrNull()?.text?.contains("[Erro de conexão") == true) continue
                if (msg.role == expectedRole) {
                    historyToSend.add(msg)
                    expectedRole = if (expectedRole == "user") "model" else "user"
                } else if (msg.role == "model" && expectedRole == "user") {
                    // Skip extra model messages
                    continue
                } else if (msg.role == "user" && expectedRole == "model") {
                    // Two users in a row. Overwrite the previous user message with this combined one
                    if (historyToSend.isNotEmpty()) {
                        val last = historyToSend.removeLast()
                        val combinedText = (last.parts.firstOrNull()?.text ?: "") + "\n" + (msg.parts.firstOrNull()?.text ?: "")
                        historyToSend.add(Content(parts = listOf(Part(text = combinedText)), role = "user"))
                    } else {
                        historyToSend.add(msg)
                        expectedRole = "model"
                    }
                }
            }

            val geminiService = GeminiService
"""
content = content.replace(old_send.strip(), new_send.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write(content)
