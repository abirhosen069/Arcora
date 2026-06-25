package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.LeaderboardEntry
import com.arcora.domain.repository.ReputationProfile
import com.arcora.domain.repository.ReputationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiReputationRepository @Inject constructor(
    private val api: ArcOraApi
) : ReputationRepository {
    override suspend fun getMyReputation(): ReputationProfile = mapApiErrors {
        val r = api.myReputation()
        ReputationProfile(
            score = r.score,
            level = r.level,
            factors = r.factors,
            sentTransactions = r.sentTransactions,
            receivedTransactions = r.receivedTransactions,
            totalVolume = r.totalVolume,
            agentWallets = r.agentWallets,
            isVerified = r.isVerified
        )
    }

    override suspend fun getLeaderboard(limit: Int): List<LeaderboardEntry> = mapApiErrors {
        api.reputationLeaderboard(limit).map {
            LeaderboardEntry(
                id = it.id,
                username = it.username,
                displayName = it.displayName,
                reputationScore = it.reputationScore,
                isVerified = it.isVerified
            )
        }
    }
}
