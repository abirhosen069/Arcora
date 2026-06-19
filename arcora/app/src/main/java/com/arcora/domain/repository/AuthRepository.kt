package com.arcora.domain.repository

import com.arcora.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    suspend fun createSmartWalletWithEmail(email: String, displayName: String): UserProfile
    suspend fun continueWithGoogle(): UserProfile
    suspend fun restoreSession(): Boolean
    suspend fun signOut()
}
