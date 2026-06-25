package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.CreateCheckoutLinkRequest
import com.arcora.data.api.CreateMerchantRequest
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.CheckoutLink
import com.arcora.domain.repository.MerchantAccount
import com.arcora.domain.repository.MerchantDashboard
import com.arcora.domain.repository.MerchantRepository
import com.arcora.domain.repository.MerchantTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiMerchantRepository @Inject constructor(
    private val api: ArcOraApi
) : MerchantRepository {
    override suspend fun createMerchant(businessName: String, merchantHandle: String, settlementAddress: String): MerchantAccount = mapApiErrors {
        val r = api.createMerchant(CreateMerchantRequest("", businessName, merchantHandle, settlementAddress))
        MerchantAccount(r.id, r.ownerId, r.businessName, r.merchantHandle, r.settlementAddress)
    }

    override suspend fun getDashboard(merchantId: String): MerchantDashboard = mapApiErrors {
        val r = api.merchantDashboard(merchantId)
        MerchantDashboard(
            merchant = MerchantAccount(r.merchant.id, r.merchant.ownerId, r.merchant.businessName, r.merchant.merchantHandle, r.merchant.settlementAddress),
            dailyVolume = r.dailyVolume,
            weeklyVolume = r.weeklyVolume,
            monthlyVolume = r.monthlyVolume,
            token = r.token,
            recentTransactions = r.recentTransactions.map { t ->
                MerchantTransaction(t.id, t.blockchainHash, t.amount, t.token, t.type, t.status)
            }
        )
    }

    override suspend fun createCheckoutLink(merchantId: String, amount: String, memo: String?, customerReference: String?): CheckoutLink = mapApiErrors {
        val r = api.createCheckoutLink(merchantId, CreateCheckoutLinkRequest(amount, memo, customerReference))
        CheckoutLink(r.checkoutId, r.merchantId, r.businessName, r.amount, r.token, r.payload, r.checkoutUrl, r.customerReference, r.status)
    }
}
