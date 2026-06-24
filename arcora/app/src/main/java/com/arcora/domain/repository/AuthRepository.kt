package com.arcora.domain.repository

import com.arcora.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    suspend fun registerStart(email: String, password: String, displayName: String, username: String): RegisterStartResult
    suspend fun registerVerify(email: String, code: String, passwordHash: String, displayName: String, username: String): UserProfile
    suspend fun login(email: String, password: String): UserProfile
    suspend fun restoreSession(): Boolean
    suspend fun signOut()
}

data class RegisterStartResult(
    val passwordHash: String,
    val email: String,
    val displayName: String,
    val username: String
)
