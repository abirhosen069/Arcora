package com.arcora.presentation.agentwallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AgentWallet
import com.arcora.domain.repository.AgentWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentWalletsUiState(
    val wallets: List<AgentWallet> = emptyList(),
    val name: String = "",
    val description: String = "",
    val monthlyBudget: String = "50.00",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: String? = null
)

@HiltViewModel
class AgentWalletsViewModel @Inject constructor(
    private val agentWalletRepository: AgentWalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgentWalletsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null, result = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, error = null, result = null) }
    fun onBudgetChange(value: String) = _uiState.update { it.copy(monthlyBudget = value.filter { c -> c.isDigit() || c == '.' }, error = null, result = null) }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { agentWalletRepository.list() }
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, wallets = list) } }
                .onFailure { t -> _uiState.update { it.copy(isLoading = false, error = t.message ?: "Could not load agent wallets") } }
        }
    }

    fun createWallet() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Enter a name for your agent wallet.") }
            return
        }
        if (state.monthlyBudget.toBigDecimalOrNull() == null) {
            _uiState.update { it.copy(error = "Enter a valid monthly budget.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching {
                agentWalletRepository.create(
                    name = state.name,
                    description = state.description.ifBlank { null },
                    monthlyBudget = state.monthlyBudget,
                    permissions = emptyList()
                )
            }
                .onSuccess { wallet ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            wallets = listOf(wallet) + it.wallets,
                            name = "",
                            description = "",
                            result = "Agent wallet '${wallet.name}' created with ${wallet.monthlyBudget} USDC/mo budget."
                        )
                    }
                }
                .onFailure { t -> _uiState.update { it.copy(isLoading = false, error = t.message ?: "Wallet creation failed") } }
        }
    }

    fun deleteWallet(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching { agentWalletRepository.delete(id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            wallets = it.wallets.filter { w -> w.id != id },
                            result = "Agent wallet deleted."
                        )
                    }
                }
                .onFailure { t -> _uiState.update { it.copy(isLoading = false, error = t.message ?: "Delete failed") } }
        }
    }
}
