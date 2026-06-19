package com.arcora.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.ai.AiActionType
import com.arcora.domain.ai.AiAssistantRepository
import com.arcora.domain.ai.AiIntent
import com.arcora.domain.usecase.BridgeToArcUseCase
import com.arcora.domain.usecase.SendPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val prompt: String = "",
    val parsedIntent: AiIntent? = null,
    val isLoading: Boolean = false,
    val isExecuting: Boolean = false,
    val executionResult: String? = null,
    val error: String? = null
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val aiAssistantRepository: AiAssistantRepository,
    private val sendPayment: SendPaymentUseCase,
    private val bridgeToArc: BridgeToArcUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState = _uiState.asStateFlow()

    fun onPromptChange(value: String) = _uiState.update { it.copy(prompt = value, error = null, executionResult = null) }

    fun parse() {
        val prompt = _uiState.value.prompt
        if (prompt.isBlank()) {
            _uiState.update { it.copy(error = "Ask ArcOra to send, request, bridge, or summarize spending.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, parsedIntent = null, executionResult = null) }
            runCatching { aiAssistantRepository.parseIntent(prompt) }
                .onSuccess { intent -> _uiState.update { it.copy(isLoading = false, parsedIntent = intent) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Could not parse request") } }
        }
    }

    fun executeParsedIntent() {
        val intent = _uiState.value.parsedIntent ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true, error = null, executionResult = null) }
            runCatching {
                when (intent.action) {
                    AiActionType.SendPayment -> {
                        val recipient = intent.recipient ?: error("Add a @username recipient before sending.")
                        val amount = intent.amount ?: error("Add a USDC amount before sending.")
                        val tx = sendPayment(recipient, amount, "Started from ArcOra AI")
                        "${tx.title} • ${tx.amount.formatted()}"
                    }
                    AiActionType.BridgeToArc -> {
                        val amount = intent.amount ?: error("Add a USDC amount before bridging.")
                        val tx = bridgeToArc.execute(intent.sourceChain ?: "Base Sepolia", amount)
                        "${tx.title} • ${tx.amount.formatted()}"
                    }
                    AiActionType.RequestPayment -> {
                        val recipient = intent.recipient ?: "recipient"
                        val amount = intent.amount?.formatted() ?: "USDC"
                        "Request prepared for $amount from $recipient. Use Receive to generate the request QR."
                    }
                    AiActionType.ShowSpending -> "Spending summary routing is ready; analytics data source is pending backend activity aggregation."
                    AiActionType.Unknown -> "I need more details before I can prepare an action."
                }
            }
                .onSuccess { result -> _uiState.update { it.copy(isExecuting = false, executionResult = result) } }
                .onFailure { throwable -> _uiState.update { it.copy(isExecuting = false, error = throwable.message ?: "Could not execute action") } }
        }
    }
}
