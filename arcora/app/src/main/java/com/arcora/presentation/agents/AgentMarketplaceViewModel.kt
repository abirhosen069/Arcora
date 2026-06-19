package com.arcora.presentation.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.data.api.AgentListingResponse
import com.arcora.data.api.AgentMarketplaceResponse
import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.mapApiErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentMarketplaceUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val agents: List<AgentListingResponse> = emptyList(),
    val policy: String? = null,
    val network: String = "Arc Testnet",
    val settlementToken: String = "USDC",
    val selectedAgent: AgentListingResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: String? = null
) {
    val visibleAgents: List<AgentListingResponse>
        get() = if (selectedCategory == "All") agents else agents.filter { it.category == selectedCategory }
}

@HiltViewModel
class AgentMarketplaceViewModel @Inject constructor(
    private val api: ArcOraApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgentMarketplaceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            runCatching { mapApiErrors { api.agentMarketplace() } }
                .onSuccess(::applyMarketplace)
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Could not load ArcOra agent marketplace."
                        )
                    }
                }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category, result = null, error = null) }
    }

    fun selectAgent(agent: AgentListingResponse) {
        _uiState.update { it.copy(selectedAgent = agent, result = null, error = null) }
    }

    fun requestDelegatedWallet(agent: AgentListingResponse) {
        _uiState.update {
            it.copy(
                selectedAgent = agent,
                result = "Delegated wallet request prepared for ${agent.name}. Production activation still requires user approval, spending policy, and wallet-provider execution credentials.",
                error = null
            )
        }
    }

    private fun applyMarketplace(response: AgentMarketplaceResponse) {
        _uiState.update {
            it.copy(
                categories = listOf("All") + response.categories.distinct(),
                selectedCategory = if (it.selectedCategory == "All" || response.categories.contains(it.selectedCategory)) it.selectedCategory else "All",
                agents = response.agents,
                policy = response.policy,
                network = response.network,
                settlementToken = response.settlementToken,
                selectedAgent = response.agents.firstOrNull { agent -> agent.id == it.selectedAgent?.id },
                isLoading = false,
                error = null
            )
        }
    }
}
