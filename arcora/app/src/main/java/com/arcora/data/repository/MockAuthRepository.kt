package com.arcora.data.repository

import com.arcora.domain.model.UserProfile
import com.arcora.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    private val userState = MutableStateFlow<UserProfile?>(null)
    override val currentUser = userState.asStateFlow()

    override suspend fun createSmartWalletWithEmail(email: String, displayName: String): UserProfile {
        delay(700)
        return UserProfile(
            id = "usr_demo",
            displayName = displayName.ifBlank { "ArcOra User" },
            username = "@${email.substringBefore('@').lowercase().filter { it.isLetterOrDigit() }.take(14).ifBlank { "arcora" }}",
            email = email,
            smartAccountAddress = "0xArc0ra000000000000000000000000000000000001",
            reputationScore = 94,
            isVerified = true
        ).also { userState.value = it }
    }

    override suspend fun continueWithGoogle(idToken: String, displayName: String, username: String, smartAccountAddress: String): UserProfile {
        delay(500)
        return createSmartWalletWithEmail("alex@arcora.test", "Alex Morgan")
    }

    override suspend fun restoreSession(): Boolean = false

    override suspend fun signOut() {
        userState.value = null
    }
}
