package com.arcora.presentation.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.model.Money
import com.arcora.domain.usecase.SendPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SendPaymentUiState(
    val recipient: String = "",
    val amount: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val result: String? = null,
    val error: String? = null
)

@HiltViewModel
class SendPaymentViewModel @Inject constructor(
    private val sendPayment: SendPaymentUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SendPaymentUiState())
    val uiState = _uiState.asStateFlow()

    fun onRecipientChange(value: String) = _uiState.update { it.copy(recipient = value, error = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }, error = null) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value, error = null) }

    fun confirmWithBiometricApproval() {
        val state = _uiState.value
        if (state.recipient.isBlank() || state.amount.toBigDecimalOrNull() == null) {
            _uiState.update { it.copy(error = "Add a recipient and valid USDC amount.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching { sendPayment(state.recipient, Money.usdc(state.amount), state.note) }
                .onSuccess { tx -> _uiState.update { it.copy(isLoading = false, result = "${tx.title} • ${tx.amount.formatted()}") } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Payment failed") } }
        }
    }
}
