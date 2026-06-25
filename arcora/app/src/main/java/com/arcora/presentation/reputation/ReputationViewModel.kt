package com.arcora.presentation.reputation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.LeaderboardEntry
import com.arcora.domain.repository.ReputationProfile
import com.arcora.domain.repository.ReputationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReputationUiState(
    val myReputation: ReputationProfile? = null,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReputationViewModel @Inject constructor(
    private val reputationRepository: ReputationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReputationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val reputation = reputationRepository.getMyReputation()
                val leaderboard = reputationRepository.getLeaderboard(10)
                reputation to leaderboard
            }
                .onSuccess { (rep, lb) ->
                    _uiState.update { it.copy(isLoading = false, myReputation = rep, leaderboard = lb) }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message ?: "Could not load reputation") }
                }
        }
    }
}
