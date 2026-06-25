package com.arcora.presentation.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.Subscription
import com.arcora.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionsUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val amount: String = "15.00",
    val interval: String = "monthly",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: String? = null
)

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }, error = null, result = null) }
    }

    fun onIntervalChange(value: String) {
        _uiState.update { it.copy(interval = value, error = null, result = null) }
    }

    fun refresh() {
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { subscriptionRepository.list(userId) }
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, subscriptions = list) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Could not load subscriptions") } }
        }
    }

    fun createDemoSubscription() {
        val userId = authRepository.currentUser.value?.id ?: return
        val state = _uiState.value
        if (state.amount.toBigDecimalOrNull() == null) {
            _uiState.update { it.copy(error = "Enter a valid subscription amount.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching {
                subscriptionRepository.create(
                    userId = userId,
                    merchantId = null,
                    agentWalletId = "demo_agent_wallet",
                    amount = state.amount,
                    interval = state.interval.ifBlank { "monthly" }
                )
            }
                .onSuccess { subscription ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            subscriptions = listOf(subscription) + it.subscriptions,
                            result = "Subscription created for ${subscription.amount} ${subscription.token}/${subscription.interval}"
                        )
                    }
                }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Subscription creation failed") } }
        }
    }

    fun pause(id: String) = lifecycleAction("Paused") { subscriptionRepository.pause(id) }
    fun renew(id: String) = lifecycleAction("Renewed") { subscriptionRepository.renew(id) }
    fun cancel(id: String) = lifecycleAction("Canceled") { subscriptionRepository.cancel(id) }

    private fun lifecycleAction(label: String, action: suspend () -> Subscription) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching { action() }
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            subscriptions = state.subscriptions.map { if (it.id == updated.id) updated else it },
                            result = "$label subscription ${updated.id.take(8)}"
                        )
                    }
                }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Subscription update failed") } }
        }
    }
}
