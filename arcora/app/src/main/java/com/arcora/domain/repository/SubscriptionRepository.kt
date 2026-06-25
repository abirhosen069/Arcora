package com.arcora.domain.repository

data class Subscription(
    val id: String,
    val userId: String,
    val merchantId: String?,
    val agentWalletId: String?,
    val amount: String,
    val token: String,
    val interval: String,
    val status: String,
    val nextChargeAt: String?
)

interface SubscriptionRepository {
    suspend fun list(userId: String): List<Subscription>
    suspend fun create(userId: String, merchantId: String?, agentWalletId: String?, amount: String, interval: String): Subscription
    suspend fun pause(id: String): Subscription
    suspend fun renew(id: String): Subscription
    suspend fun cancel(id: String): Subscription
}
