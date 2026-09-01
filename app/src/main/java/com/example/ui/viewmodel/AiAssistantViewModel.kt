package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BackendResult
import com.example.data.repository.BackendWorkforceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiChatMessage(val fromUser: Boolean, val text: String)

data class AiAssistantUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

/** Thin client for the `ai-assistant` Edge Function — shared by worker and supervisor dashboards. */
class AiAssistantViewModel(private val repository: BackendWorkforceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMessage = AiChatMessage(fromUser = true, text = text.trim())
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + userMessage, isSending = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.askAssistant(text.trim())) {
                is BackendResult.Success -> _uiState.value = _uiState.value.copy(
                    isSending = false, messages = _uiState.value.messages + AiChatMessage(fromUser = false, text = result.value)
                )
                is BackendResult.Failure -> _uiState.value = _uiState.value.copy(isSending = false, errorMessage = result.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}
