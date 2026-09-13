import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

new_loop = """
            val historyToSend = mutableListOf<Content>()
            var expectedRole = "user"
            for (msg in rawHistory) {
                val text = msg.parts.firstOrNull()?.text ?: ""
                // Skip empty messages or error messages
                if (text.isBlank() || text.contains("[Erro de conexão")) continue
                
                if (msg.role == expectedRole) {
                    historyToSend.add(msg)
                    expectedRole = if (expectedRole == "user") "model" else "user"
                } else if (msg.role == "user" && expectedRole == "model") {
                    // Combine with previous user message
                    if (historyToSend.isNotEmpty() && historyToSend.last().role == "user") {
                        val last = historyToSend.removeLast()
                        val combinedText = (last.parts.firstOrNull()?.text ?: "") + "\\n" + text
                        historyToSend.add(Content(parts = listOf(Part(text = combinedText)), role = "user"))
                    }
                } else if (msg.role == "model" && expectedRole == "user") {
                    // Combine with previous model message
                    if (historyToSend.isNotEmpty() && historyToSend.last().role == "model") {
                        val last = historyToSend.removeLast()
                        val combinedText = (last.parts.firstOrNull()?.text ?: "") + "\\n" + text
                        historyToSend.add(Content(parts = listOf(Part(text = combinedText)), role = "model"))
                    }
                }
            }
            
            if (historyToSend.isNotEmpty() && historyToSend.last().role == "model") {
                historyToSend.removeLast()
            }
            
            if (historyToSend.isEmpty() && rawHistory.isNotEmpty()) {
                val lastUserMsg = rawHistory.lastOrNull { it.role == "user" }
                if (lastUserMsg != null) {
                    historyToSend.add(lastUserMsg)
                }
            }
"""

content = re.sub(r'val historyToSend = mutableListOf<Content>\(\).*?val geminiService = GeminiService', new_loop + '\n            val geminiService = GeminiService', content, flags=re.DOTALL)

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write(content)
