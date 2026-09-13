import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("import kotlinx.coroutines.flow.asStateFlow", "import kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.flow.MutableSharedFlow\nimport kotlinx.coroutines.flow.SharedFlow\nimport kotlinx.coroutines.flow.asSharedFlow")

event_decl = """
    private val _addPerfumeEvent = MutableSharedFlow<Triple<String, String, String>>(extraBufferCapacity = 1)
    val addPerfumeEvent: SharedFlow<Triple<String, String, String>> = _addPerfumeEvent.asSharedFlow()
"""
content = content.replace("    fun sendMessage(text: String) {", event_decl + "\n    fun sendMessage(text: String) {")

old_handler = """
                val currentList = _chatHistory.value.toMutableList()
                val currentText = currentList[assistantMsgIndex].parts.firstOrNull()?.text ?: ""
                currentList[assistantMsgIndex] = Content(
                    parts = listOf(Part(text = currentText + token)),
                    role = "model"
                )
                _chatHistory.value = currentList
"""

new_handler = """
                val currentList = _chatHistory.value.toMutableList()
                var currentText = currentList[assistantMsgIndex].parts.firstOrNull()?.text ?: ""
                currentText += token
                
                val commandRegex = Regex("\\[ADD_PERFUME:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]")
                val match = commandRegex.find(currentText)
                
                if (match != null) {
                    val name = match.groupValues[1].trim()
                    val brand = match.groupValues[2].trim()
                    var status = match.groupValues[3].trim()
                    
                    if (status.contains("Quero", ignoreCase = true)) status = "Quero ter"
                    else if (status.contains("Já possuo", ignoreCase = true)) status = "Já possuo"
                    else if (status.contains("Pipeline", ignoreCase = true)) status = "Pipeline"
                    else status = "Quero ter"
                    
                    currentText = currentText.replace(match.value, "").trim()
                    _addPerfumeEvent.tryEmit(Triple(name, brand, status))
                }
                
                currentList[assistantMsgIndex] = Content(
                    parts = listOf(Part(text = currentText)),
                    role = "model"
                )
                _chatHistory.value = currentList
"""
content = content.replace(old_handler, new_handler)

with open("app/src/main/java/com/aistudio/perfumatico/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write(content)

