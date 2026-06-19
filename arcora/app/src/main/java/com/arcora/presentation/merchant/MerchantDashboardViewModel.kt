package com.arcora.presentation.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.CheckoutLinkResponse
import com.arcora.data.api.CreateCheckoutLinkRequest
import com.arcora.data.api.CreateMerchantRequest
import com.arcora.data.api.MerchantAccountResponse
import com.arcora.data.api.MerchantDashboardResponse
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantDashboardUiState(
    val merchant: MerchantAccountResponse? = null,
    val dashboard: MerchantDashboardResponse? = null,
    val checkout: CheckoutLinkResponse? = null,
    val checkoutAmount: String = "25.00",
    val checkoutMemo: String = "ArcOra checkout",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MerchantDashboardViewModel @Inject constructor(
    private val api: ArcOraApi,
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
        val ownerId = user?.id ?: "demo_owner"
        val address = user?.smartAccountAddress ?: "0x0000000000000000000000000000000000000000"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                mapApiErrors {
                    val merchant = api.createMerchant(
                        CreateMerchantRequest(
                            ownerId = ownerId,
                            businessName = "ArcOra Shop",
                            merchantHandle = "@arcora_shop",
                            settlementAddress = address
                        )
                    )
                    val dashboard = api.merchantDashboard(merchant.id)
                    merchant to dashboard
                }
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
            runCatching { mapApiErrors { api.merchantDashboard(merchantId) } }
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
                mapApiErrors {
                    api.createCheckoutLink(
                        merchantId,
                        CreateCheckoutLinkRequest(
                            amount = state.checkoutAmount,
                            memo = state.checkoutMemo.ifBlank { null },
                            customerReference = "mobile_demo"
                        )
                    )
                }
            }
                .onSuccess { checkout -> _uiState.update { it.copy(isLoading = false, checkout = checkout, error = null) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Checkout link failed") } }
        }
    }
}
