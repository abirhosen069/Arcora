package com.arcora.domain.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val smartAccountAddress: String,
    val avatarUrl: String? = null,
    val reputationScore: Int = 0,
    val isVerified: Boolean = false
)
