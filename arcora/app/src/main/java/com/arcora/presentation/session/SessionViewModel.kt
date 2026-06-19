package com.arcora.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data class Ready(val hasSession: Boolean) : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val restored = runCatching { authRepository.restoreSession() }.getOrDefault(false)
            _uiState.value = SessionUiState.Ready(restored)
        }
    }
}
