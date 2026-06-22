package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.GoogleAuthRequest
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

        val response = api.signup(
            SignupRequest(
                email = normalizedEmail,
                displayName = safeName,
                username = username
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

    override suspend fun continueWithGoogle(idToken: String, displayName: String, username: String): UserProfile = mapApiErrors {
        val response = api.googleAuth(
            GoogleAuthRequest(
                idToken = idToken,
                displayName = displayName,
                username = username,
                smartAccountAddress = ""
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
}
