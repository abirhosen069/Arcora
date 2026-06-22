package com.arcora.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.usecase.CreateSmartWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val displayName: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val walletReady: Boolean = false,
    val authMethod: AuthMethod = AuthMethod.EMAIL
)

enum class AuthMethod { EMAIL, GOOGLE, PASSKEY }

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createSmartWallet: CreateSmartWalletUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun selectAuthMethod(method: AuthMethod) = _uiState.update { it.copy(authMethod = method, error = null) }

    fun createWallet() {
        val state = _uiState.value
        if (!state.email.contains("@")) {
            _uiState.update { it.copy(error = "Enter a valid email to create your ArcOra smart account.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { createSmartWallet(state.email.trim(), state.displayName.trim()) }
                .onSuccess { _uiState.update { it.copy(isLoading = false, walletReady = true) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Wallet creation failed") } }
        }
    }

    fun onGoogleSignInResult(idToken: String, displayName: String) {
        val username = displayName.lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(20).ifBlank { "arcora" }
        val smartAddress = deterministicAddress(idToken)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { authRepository.continueWithGoogle(idToken, displayName, username, smartAddress) }
                .onSuccess { _uiState.update { it.copy(isLoading = false, walletReady = true) } }
                .onFailure { throwable -> _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Google sign-in failed") } }
        }
    }

    private fun deterministicAddress(seed: String): String {
        val hex = seed.encodeToByteArray().joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        return "0x${hex.padEnd(40, '0').take(40)}"
    }
}
