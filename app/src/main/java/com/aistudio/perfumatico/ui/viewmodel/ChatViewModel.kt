package com.aistudio.perfumatico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.perfumatico.data.remote.Content
import com.aistudio.perfumatico.data.remote.Part
import com.aistudio.perfumatico.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _chatHistory = MutableStateFlow<List<Content>>(emptyList())
    val chatHistory: StateFlow<List<Content>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

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
                val currentText = currentList[assistantMsgIndex].parts.firstOrNull()?.text ?: ""
                currentList[assistantMsgIndex] = Content(
                    parts = listOf(Part(text = currentText + token)),
                    role = "model"
                )
                _chatHistory.value = currentList
            }
        }
    }
}
