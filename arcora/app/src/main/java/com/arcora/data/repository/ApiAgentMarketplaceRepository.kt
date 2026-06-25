package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.AgentListing
import com.arcora.domain.repository.AgentMarketplaceRepository
import com.arcora.domain.repository.MarketplaceResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAgentMarketplaceRepository @Inject constructor(
    private val api: ArcOraApi
) : AgentMarketplaceRepository {
    override suspend fun getMarketplace(): MarketplaceResult = mapApiErrors {
        val r = api.agentMarketplace()
        MarketplaceResult(
            categories = r.categories,
            agents = r.agents.map { a ->
                AgentListing(
                    id = a.id,
                    name = a.name,
                    category = a.category,
                    description = a.description,
                    monthlyBudget = a.monthlyBudget,
                    token = a.token,
                    reputationLabel = a.reputationLabel,
                    riskLevel = a.riskLevel,
                    permissions = a.permissions
                )
            },
            settlementToken = r.settlementToken,
            network = r.network,
            policy = r.policy
        )
    }
}
