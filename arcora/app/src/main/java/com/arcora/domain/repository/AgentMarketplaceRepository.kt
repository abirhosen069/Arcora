package com.arcora.domain.repository

data class AgentListing(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val monthlyBudget: String,
    val token: String,
    val reputationLabel: String,
    val riskLevel: String,
    val permissions: List<String>
)

data class MarketplaceResult(
    val categories: List<String>,
    val agents: List<AgentListing>,
    val settlementToken: String,
    val network: String,
    val policy: String?
)

interface AgentMarketplaceRepository {
    suspend fun getMarketplace(): MarketplaceResult
}
