package com.arcora.domain.repository

data class ReputationProfile(
    val score: Int,
    val level: String,
    val factors: List<String>,
    val sentTransactions: Int,
    val receivedTransactions: Int,
    val totalVolume: String,
    val agentWallets: Int,
    val isVerified: Boolean
)

data class LeaderboardEntry(
    val id: String,
    val username: String,
    val displayName: String,
    val reputationScore: Int,
    val isVerified: Boolean
)

interface ReputationRepository {
    suspend fun getMyReputation(): ReputationProfile
    suspend fun getLeaderboard(limit: Int = 10): List<LeaderboardEntry>
}
