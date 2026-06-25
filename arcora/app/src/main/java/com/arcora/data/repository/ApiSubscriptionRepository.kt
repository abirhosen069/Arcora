package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.CreateSubscriptionRequest
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.Subscription
import com.arcora.domain.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiSubscriptionRepository @Inject constructor(
    private val api: ArcOraApi
) : SubscriptionRepository {
    override suspend fun list(userId: String): List<Subscription> = mapApiErrors {
        api.subscriptions(userId).map { it.toDomain() }
    }

    override suspend fun create(userId: String, merchantId: String?, agentWalletId: String?, amount: String, interval: String): Subscription = mapApiErrors {
        api.createSubscription(CreateSubscriptionRequest(userId, merchantId, agentWalletId, amount, interval)).toDomain()
    }

    override suspend fun pause(id: String): Subscription = mapApiErrors {
        api.pauseSubscription(id).toDomain()
    }

    override suspend fun renew(id: String): Subscription = mapApiErrors {
        api.renewSubscription(id).toDomain()
    }

    override suspend fun cancel(id: String): Subscription = mapApiErrors {
        api.cancelSubscription(id).toDomain()
    }

    private fun com.arcora.data.api.SubscriptionResponse.toDomain() = Subscription(
        id = id,
        userId = userId,
        merchantId = merchantId,
        agentWalletId = agentWalletId,
        amount = amount,
        token = token,
        interval = interval,
        status = status,
        nextChargeAt = nextChargeAt
    )
}
