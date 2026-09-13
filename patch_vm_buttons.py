import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

# Replace stream handler regex to just ignore ASK_COLLECTION during stream (or keep it in the text so UI parses it)
old_handler = 'val commandRegex = Regex("""\\[ADD_PERFUME:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]""")'
new_handler = 'val commandRegex = Regex("""\\[ADD_PERFUME:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]""")' 
# Actually we can just leave ADD_PERFUME there as a fallback, but let's add addPerfumeFromUI

add_func = """
    fun addPerfumeFromUI(name: String, brand: String, status: String, messageIndex: Int) {
        _addPerfumeEvent.tryEmit(Triple(name, brand, status))
        
        val currentList = _chatHistory.value.toMutableList()
        if (messageIndex in currentList.indices) {
            val msg = currentList[messageIndex]
            val text = msg.parts.firstOrNull()?.text ?: ""
            val cleanText = text.replace(Regex("\\[ASK_COLLECTION:.*?\\]"), "").trim()
            currentList[messageIndex] = Content(parts = listOf(Part(text = cleanText)), role = msg.role)
            _chatHistory.value = currentList
        }
    }
"""

content = content.replace("    fun sendMessage(text: String) {", add_func + "\n    fun sendMessage(text: String) {")

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write(content)
