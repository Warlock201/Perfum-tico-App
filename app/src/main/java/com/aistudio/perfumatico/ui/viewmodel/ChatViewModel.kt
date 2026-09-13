package com.aistudio.perfumatico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.perfumatico.data.remote.Content
import com.aistudio.perfumatico.data.remote.Part
import com.aistudio.perfumatico.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _chatHistory = MutableStateFlow<List<Content>>(emptyList())
    val chatHistory: StateFlow<List<Content>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()


    private val _addPerfumeEvent = MutableSharedFlow<Triple<String, String, String>>(extraBufferCapacity = 1)
    val addPerfumeEvent: SharedFlow<Triple<String, String, String>> = _addPerfumeEvent.asSharedFlow()


    fun addPerfumeFromUI(name: String, brand: String, status: String, messageIndex: Int) {
        _addPerfumeEvent.tryEmit(Triple(name, brand, status))
        
        val currentList = _chatHistory.value.toMutableList()
        if (messageIndex in currentList.indices) {
            val msg = currentList[messageIndex]
            val text = msg.parts.firstOrNull()?.text ?: ""
            val cleanText = text.replace(Regex("""\[ASK_COLLECTION:.*?\]"""), "").trim()
            currentList[messageIndex] = Content(parts = listOf(Part(text = cleanText)), role = msg.role)
            _chatHistory.value = currentList
        }
    }

    fun sendMessage(text: String) {
        val userMsg = Content(
            parts = listOf(Part(text = text)),
            role = "user"
        )
        _chatHistory.value = _chatHistory.value + userMsg
        _isChatLoading.value = true

        val assistantMsgIndex = _chatHistory.value.size
        _chatHistory.value = _chatHistory.value + Content(
            parts = listOf(Part(text = "")),
            role = "model"
        )

        viewModelScope.launch {
            val historyToSend = _chatHistory.value.take(assistantMsgIndex)
            val geminiService = GeminiService
            geminiService.chatWithSommelier(historyToSend) { token ->
                _isChatLoading.value = false
                val currentList = _chatHistory.value.toMutableList()
                var currentText = currentList[assistantMsgIndex].parts.firstOrNull()?.text ?: ""
                currentText += token
                
                val commandRegex = Regex("""\[ADD_PERFUME:\s*(.+?)\s*\|\s*(.+?)\s*\|\s*(.+?)\s*\]""")
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
            }
        }
    }
}
