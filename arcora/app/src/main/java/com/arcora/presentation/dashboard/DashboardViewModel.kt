package com.arcora.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.model.UserProfile
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.model.Portfolio
import com.arcora.domain.model.TransactionRecord
import com.arcora.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val portfolio: Portfolio? = null,
    val activity: List<TransactionRecord> = emptyList(),
    val isRefreshing: Boolean = false,
    val liveBalanceError: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeDashboard: ObserveDashboardUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val refreshState = MutableStateFlow(DashboardRefreshState())
    val uiState: StateFlow<DashboardUiState> = combine(
        observeDashboard.portfolio(),
        observeDashboard.activity(),
        refreshState.asStateFlow()
    ) { portfolio, activity, refresh ->
        DashboardUiState(portfolio, activity, refresh.isRefreshing, refresh.error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    val currentUser: UserProfile?
        get() = authRepository.currentUser.value

    init {
        refreshLiveBalance()
    }

    fun refreshLiveBalance() {
        viewModelScope.launch {
            refreshState.update { it.copy(isRefreshing = true, error = null) }
            val address = currentUser?.smartAccountAddress ?: DEFAULT_DEMO_ADDRESS

            runCatching { observeDashboard.refreshPortfolio(address) }
                .onSuccess { refreshState.update { it.copy(isRefreshing = false, error = null) } }
                .onFailure { throwable ->
                    refreshState.update {
                        it.copy(
                            isRefreshing = false,
                            error = throwable.message ?: "Live balance unavailable"
                        )
                    }
                }
        }
    }

    private data class DashboardRefreshState(
        val isRefreshing: Boolean = false,
        val error: String? = null
    )

    private companion object {
        const val DEFAULT_DEMO_ADDRESS = "0x0000000000000000000000000000000000000000"
    }
}
