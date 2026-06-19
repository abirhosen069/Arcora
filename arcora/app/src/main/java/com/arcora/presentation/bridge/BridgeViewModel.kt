package com.arcora.presentation.bridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.model.BridgeQuote
import com.arcora.domain.model.Money
import com.arcora.domain.usecase.BridgeToArcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BridgeUiState(
    val sourceChain: String = "Base Sepolia",
    val amount: String = "",
    val quote: BridgeQuote? = null,
    val isLoading: Boolean = false,
    val result: String? = null,
    val error: String? = null
)

@HiltViewModel
class BridgeViewModel @Inject constructor(
    private val bridgeToArc: BridgeToArcUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(BridgeUiState())
    val uiState = _uiState.asStateFlow()

    fun onSourceChainChange(value: String) = _uiState.update { it.copy(sourceChain = value, quote = null, error = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }, quote = null, error = null) }

    fun previewRoute() {
        val state = _uiState.value
        if (state.amount.toBigDecimalOrNull() == null) {
            _uiState.update { it.copy(error = "Enter a valid USDC amount to bridge.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { bridgeToArc.quote(state.sourceChain, Money.usdc(state.amount)) }
                .onSuccess { quote -> _uiState.update { it.copy(isLoading = false, quote = quote) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Could not quote bridge") } }
        }
    }

    fun executeBridge() {
        val quote = _uiState.value.quote ?: return previewRoute()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching { bridgeToArc.execute(quote.sourceChain, quote.amount) }
                .onSuccess { tx -> _uiState.update { it.copy(isLoading = false, result = "${tx.title}: ${tx.amount.formatted()}") } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Bridge failed") } }
        }
    }
}
