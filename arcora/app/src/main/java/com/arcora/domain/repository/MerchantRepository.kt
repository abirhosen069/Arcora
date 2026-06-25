package com.arcora.domain.repository

data class MerchantAccount(
    val id: String,
    val ownerId: String,
    val businessName: String,
    val merchantHandle: String,
    val settlementAddress: String
)

data class MerchantDashboard(
    val merchant: MerchantAccount,
    val dailyVolume: String,
    val weeklyVolume: String,
    val monthlyVolume: String,
    val token: String,
    val recentTransactions: List<MerchantTransaction>
)

data class MerchantTransaction(
    val id: String,
    val blockchainHash: String?,
    val amount: String,
    val token: String,
    val type: String,
    val status: String
)

data class CheckoutLink(
    val checkoutId: String,
    val merchantId: String,
    val businessName: String,
    val amount: String,
    val token: String,
    val payload: String,
    val checkoutUrl: String,
    val customerReference: String?,
    val status: String
)

interface MerchantRepository {
    suspend fun createMerchant(businessName: String, merchantHandle: String, settlementAddress: String): MerchantAccount
    suspend fun getDashboard(merchantId: String): MerchantDashboard
    suspend fun createCheckoutLink(merchantId: String, amount: String, memo: String?, customerReference: String?): CheckoutLink
}
