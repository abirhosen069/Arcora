package com.arcora.presentation.receive

import androidx.lifecycle.ViewModel
import com.arcora.domain.model.Money
import com.arcora.domain.qr.PaymentQrPayload
import com.arcora.domain.qr.PaymentQrPayloadGenerator
import com.arcora.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

private const val FALLBACK_USERNAME = "@arcora"
private const val FALLBACK_ADDRESS = "0x0000000000000000000000000000000000000000"

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val qrPayloadGenerator: PaymentQrPayloadGenerator
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ReceiveUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }, error = null) }
    }

    fun onMemoChange(value: String) {
        _uiState.update { it.copy(memo = value, error = null) }
    }

    fun useStaticQr() {
        val user = authRepository.currentUser.value
        val username = user?.username ?: FALLBACK_USERNAME
        val address = user?.smartAccountAddress ?: FALLBACK_ADDRESS
        _uiState.update {
            it.copy(
                username = username,
                smartAccountAddress = address,
                activePayload = qrPayloadGenerator.staticReceive(username, address),
                isPaymentRequest = false,
                error = null
            )
        }
    }

    fun usePaymentRequestQr() {
        val state = _uiState.value
        val amount = state.amount.toBigDecimalOrNull()
        if (amount == null || amount.signum() <= 0) {
            _uiState.update { it.copy(error = "Enter a valid USDC amount for the request QR.") }
            return
        }

        val user = authRepository.currentUser.value
        val username = user?.username ?: state.username
        val address = user?.smartAccountAddress ?: state.smartAccountAddress
        val payload = qrPayloadGenerator.paymentRequest(
            PaymentQrPayload(
                username = username,
                smartAccountAddress = address,
                amount = Money.usdc(amount.toPlainString()),
                memo = state.memo.ifBlank { null },
                requestId = "req_${UUID.randomUUID()}"
            )
        )

        _uiState.update {
            it.copy(
                username = username,
                smartAccountAddress = address,
                activePayload = payload,
                isPaymentRequest = true,
                error = null
            )
        }
    }

    private fun initialState(): ReceiveUiState {
        val user = authRepository.currentUser.value
        val username = user?.username ?: FALLBACK_USERNAME
        val address = user?.smartAccountAddress ?: FALLBACK_ADDRESS
        return ReceiveUiState(
            username = username,
            smartAccountAddress = address,
            activePayload = qrPayloadGenerator.staticReceive(username, address)
        )
    }
}

data class ReceiveUiState(
    val username: String,
    val smartAccountAddress: String,
    val amount: String = "",
    val memo: String = "",
    val activePayload: String,
    val isPaymentRequest: Boolean = false,
    val error: String? = null
)
