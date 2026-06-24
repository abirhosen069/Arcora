package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.LoginRequest
import com.arcora.data.api.RegisterStartRequest
import com.arcora.data.api.RegisterVerifyRequest
import com.arcora.data.api.UserProfileResponse
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.model.UserProfile
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.RegisterStartResult
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

    override suspend fun registerStart(email: String, password: String, displayName: String, username: String): RegisterStartResult = mapApiErrors {
        val normalizedEmail = email.trim().lowercase()
        val safeName = displayName.trim().ifBlank { "ArcOra User" }
        val safeUsername = username.trim().ifBlank { normalizedEmail.substringBefore('@') }
            .lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(20).ifBlank { "arcora" }

        val response = api.registerStart(
            RegisterStartRequest(
                email = normalizedEmail,
                password = password,
                displayName = safeName,
                username = safeUsername
            )
        )

        RegisterStartResult(
            passwordHash = response.passwordHash,
            email = response.email,
            displayName = response.displayName,
            username = response.username
        )
    }

    override suspend fun registerVerify(email: String, code: String, passwordHash: String, displayName: String, username: String): UserProfile = mapApiErrors {
        val response = api.registerVerify(
            RegisterVerifyRequest(
                email = email,
                code = code,
                passwordHash = passwordHash,
                displayName = displayName,
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

    override suspend fun login(email: String, password: String): UserProfile = mapApiErrors {
        val response = api.login(
            LoginRequest(
                email = email.trim().lowercase(),
                password = password
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
