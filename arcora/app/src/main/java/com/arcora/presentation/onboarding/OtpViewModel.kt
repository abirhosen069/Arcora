package com.arcora.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtpUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val verified: Boolean = false
)

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState = _uiState.asStateFlow()

    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value.filter { c -> c.isLetterOrDigit() }.uppercase(), error = null) }

    fun verify(email: String, passwordHash: String, displayName: String, username: String) {
        val code = _uiState.value.code
        if (code.length < 4) {
            _uiState.update { it.copy(error = "Enter the verification code from your email.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                authRepository.registerVerify(email, code, passwordHash, displayName, username)
            }
                .onSuccess { _uiState.update { it.copy(isLoading = false, verified = true) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Verification failed") } }
        }
    }
}
