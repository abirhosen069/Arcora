package com.arcora.presentation.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.CheckoutLink
import com.arcora.domain.repository.MerchantAccount
import com.arcora.domain.repository.MerchantDashboard
import com.arcora.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantDashboardUiState(
    val merchant: MerchantAccount? = null,
    val dashboard: MerchantDashboard? = null,
    val checkout: CheckoutLink? = null,
    val checkoutAmount: String = "25.00",
    val checkoutMemo: String = "ArcOra checkout",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MerchantDashboardViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MerchantDashboardUiState())
    val uiState = _uiState.asStateFlow()

    fun onCheckoutAmountChange(value: String) {
        _uiState.update { it.copy(checkoutAmount = value.filter { char -> char.isDigit() || char == '.' }, error = null) }
    }

    fun onCheckoutMemoChange(value: String) {
        _uiState.update { it.copy(checkoutMemo = value, error = null) }
    }

    fun createDemoMerchant() {
        val user = authRepository.currentUser.value
        val address = user?.smartAccountAddress ?: "0x0000000000000000000000000000000000000000"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val merchant = merchantRepository.createMerchant(
                    businessName = "ArcOra Shop",
                    merchantHandle = "@arcora_shop",
                    settlementAddress = address
                )
                val dashboard = merchantRepository.getDashboard(merchant.id)
                merchant to dashboard
            }
                .onSuccess { (merchant, dashboard) ->
                    _uiState.update { it.copy(isLoading = false, merchant = merchant, dashboard = dashboard, error = null) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Merchant setup failed") }
                }
        }
    }

    fun refreshDashboard() {
        val merchantId = _uiState.value.merchant?.id
        if (merchantId == null) {
            _uiState.update { it.copy(error = "Create or load a merchant account first.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { merchantRepository.getDashboard(merchantId) }
                .onSuccess { dashboard ->
                    _uiState.update { it.copy(isLoading = false, dashboard = dashboard, error = null) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Merchant dashboard refresh failed") }
                }
        }
    }

    fun createCheckoutLink() {
        val merchantId = _uiState.value.merchant?.id
        if (merchantId == null) {
            _uiState.update { it.copy(error = "Create or load a merchant account first.") }
            return
        }
        val state = _uiState.value
        if (state.checkoutAmount.toBigDecimalOrNull() == null) {
            _uiState.update { it.copy(error = "Enter a valid checkout amount.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                merchantRepository.createCheckoutLink(
                    merchantId = merchantId,
                    amount = state.checkoutAmount,
                    memo = state.checkoutMemo.ifBlank { null },
                    customerReference = "mobile_demo"
                )
            }
                .onSuccess { checkout -> _uiState.update { it.copy(isLoading = false, checkout = checkout, error = null) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Checkout link failed") } }
        }
    }
}
