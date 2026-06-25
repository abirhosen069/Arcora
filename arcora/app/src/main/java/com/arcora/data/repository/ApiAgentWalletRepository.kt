package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.CreateAgentWalletRequest
import com.arcora.data.api.UpdateAgentWalletRequest
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.AgentWallet
import com.arcora.domain.repository.AgentWalletRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAgentWalletRepository @Inject constructor(
    private val api: ArcOraApi
) : AgentWalletRepository {
    override suspend fun list(): List<AgentWallet> = mapApiErrors {
        api.listAgentWallets().map { it.toDomain() }
    }

    override suspend fun get(id: String): AgentWallet = mapApiErrors {
        api.getAgentWallet(id).toDomain()
    }

    override suspend fun create(name: String, description: String?, monthlyBudget: String, permissions: List<String>): AgentWallet = mapApiErrors {
        api.createAgentWallet(CreateAgentWalletRequest(name, description, monthlyBudget, permissions)).toDomain()
    }

    override suspend fun update(id: String, name: String?, description: String?, monthlyBudget: String?, permissions: List<String>?): AgentWallet = mapApiErrors {
        api.updateAgentWallet(id, UpdateAgentWalletRequest(name, description, monthlyBudget, permissions)).toDomain()
    }

    override suspend fun delete(id: String) = mapApiErrors {
        api.deleteAgentWallet(id)
    }

    private fun com.arcora.data.api.AgentWalletResponse.toDomain() = AgentWallet(
        id = id,
        ownerId = ownerId,
        name = name,
        description = description,
        walletAddress = walletAddress,
        monthlyBudget = monthlyBudget,
        permissions = permissions,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
