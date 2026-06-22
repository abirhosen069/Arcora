package com.arcora.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.LeaderboardEntry
import com.arcora.data.api.ReputationResponse
import com.arcora.data.api.UserProfileResponse
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val profile: UserProfileResponse? = null,
    val reputation: ReputationResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: ArcOraApi,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val profile = mapApiErrors { api.me() }
                val reputation = mapApiErrors { api.myReputation() }
                profile to reputation
            }
                .onSuccess { (profile, rep) ->
                    _uiState.update { it.copy(isLoading = false, profile = profile, reputation = rep) }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message ?: "Could not load profile") }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(signedOut = true) }
        }
    }
}
