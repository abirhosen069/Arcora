package com.arcora.data.repository

import com.arcora.domain.model.UserProfile
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.RegisterStartResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    private val userState = MutableStateFlow<UserProfile?>(null)
    override val currentUser = userState.asStateFlow()

    override suspend fun registerStart(email: String, password: String, displayName: String, username: String): RegisterStartResult {
        delay(700)
        return RegisterStartResult(
            passwordHash = "mock_hash",
            email = email,
            displayName = displayName.ifBlank { "ArcOra User" },
            username = "@${email.substringBefore('@').lowercase().filter { it.isLetterOrDigit() }.take(14).ifBlank { "arcora" }}"
        )
    }

    override suspend fun registerVerify(email: String, code: String, passwordHash: String, displayName: String, username: String): UserProfile {
        delay(500)
        return UserProfile(
            id = "usr_demo",
            displayName = displayName.ifBlank { "ArcOra User" },
            username = username,
            email = email,
            smartAccountAddress = "0xArc0ra000000000000000000000000000000000001",
            reputationScore = 94,
            isVerified = true
        ).also { userState.value = it }
    }

    override suspend fun login(email: String, password: String): UserProfile {
        delay(500)
        return UserProfile(
            id = "usr_demo",
            displayName = "ArcOra User",
            username = "@${email.substringBefore('@').lowercase().filter { it.isLetterOrDigit() }.take(14).ifBlank { "arcora" }}",
            email = email,
            smartAccountAddress = "0xArc0ra000000000000000000000000000000000001",
            reputationScore = 94,
            isVerified = true
        ).also { userState.value = it }
    }

    override suspend fun restoreSession(): Boolean = false

    override suspend fun signOut() {
        userState.value = null
    }
}
