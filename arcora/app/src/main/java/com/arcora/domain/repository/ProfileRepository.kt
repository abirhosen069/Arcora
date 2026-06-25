package com.arcora.domain.repository

data class UserProfile(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val smartAccountAddress: String,
    val reputationScore: Int,
    val isVerified: Boolean,
    val profileImageUrl: String? = null
)

interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun updateProfileImage(imageBase64: String, mimeType: String): String?
}
