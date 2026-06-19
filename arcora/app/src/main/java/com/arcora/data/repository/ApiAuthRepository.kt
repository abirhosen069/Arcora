package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.SignupRequest
import com.arcora.data.api.UserProfileResponse
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.model.UserProfile
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.security.SecureSessionStore
import com.arcora.domain.security.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAuthRepository @Inject constructor(
    private val api: ArcOraApi,
    private val sessionStore: SecureSessionStore
) : AuthRepository {
    private val userState = MutableStateFlow<UserProfile?>(null)
    override val currentUser = userState.asStateFlow()

    override suspend fun createSmartWalletWithEmail(email: String, displayName: String): UserProfile = mapApiErrors {
        val normalizedEmail = email.trim().lowercase()
        val safeName = displayName.trim().ifBlank { "ArcOra User" }
        val username = normalizedEmail.substringBefore('@')
            .lowercase()
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(20)
            .ifBlank { "arcora" }
        val smartAccountAddress = deterministicDemoAddress(normalizedEmail)

        val response = api.signup(
            SignupRequest(
                email = normalizedEmail,
                displayName = safeName,
                username = username,
                smartAccountAddress = smartAccountAddress
            )
        )

        sessionStore.save(
            SessionToken(
                accessToken = response.session.accessToken,
                refreshToken = response.session.refreshToken,
                expiresAtEpochMillis = response.session.expiresAtEpochMillis
            )
        )

        response.user.toDomain().also { userState.value = it }
    }

    override suspend fun restoreSession(): Boolean = mapApiErrors {
        if (sessionStore.read() == null) {
            return@mapApiErrors false
        }
        api.me().toDomain().also { userState.value = it }
        true
    }

    override suspend fun continueWithGoogle(): UserProfile {
        // Google identity selection is still a UI/auth-provider integration task. This keeps
        // the app API-backed instead of mock-only while provider credentials are pending.
        return createSmartWalletWithEmail("google.user@arcora.test", "Google ArcOra User")
    }

    override suspend fun signOut() {
        sessionStore.clear()
        userState.value = null
    }

    private fun UserProfileResponse.toDomain() = UserProfile(
        id = id,
        displayName = displayName,
        username = username,
        email = email,
        smartAccountAddress = smartAccountAddress,
        avatarUrl = profileImageUrl,
        reputationScore = reputationScore,
        isVerified = isVerified
    )

    private fun deterministicDemoAddress(seed: String): String {
        val hex = seed.encodeToByteArray().joinToString(separator = "") { byte ->
            byte.toUByte().toString(16).padStart(2, '0')
        }
        return "0x${hex.padEnd(40, '0').take(40)}"
    }
}
