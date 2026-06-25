package com.arcora.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.ProfileRepository
import com.arcora.domain.repository.ReputationRepository
import com.arcora.domain.repository.UserProfile
import com.arcora.domain.repository.ReputationProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val profile: UserProfile? = null,
    val reputation: ReputationProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val reputationRepository: ReputationRepository,
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
                val profile = profileRepository.getProfile()
                val reputation = reputationRepository.getMyReputation()
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
