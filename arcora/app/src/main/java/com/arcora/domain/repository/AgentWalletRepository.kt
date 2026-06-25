package com.arcora.domain.repository

data class AgentWallet(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String?,
    val walletAddress: String,
    val monthlyBudget: String,
    val permissions: List<String>,
    val createdAt: String,
    val updatedAt: String
)

interface AgentWalletRepository {
    suspend fun list(): List<AgentWallet>
    suspend fun get(id: String): AgentWallet
    suspend fun create(name: String, description: String?, monthlyBudget: String, permissions: List<String>): AgentWallet
    suspend fun update(id: String, name: String?, description: String?, monthlyBudget: String?, permissions: List<String>?): AgentWallet
    suspend fun delete(id: String)
}
