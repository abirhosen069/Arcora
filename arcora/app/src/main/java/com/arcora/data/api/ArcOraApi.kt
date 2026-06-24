package com.arcora.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ArcOraApi {
    @POST("auth/register/start")
    suspend fun registerStart(@Body request: RegisterStartRequest): RegisterStartResponse

    @POST("auth/register/verify")
    suspend fun registerVerify(@Body request: RegisterVerifyRequest): AuthSessionResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthSessionResponse

    @POST("auth/otp/register")
    suspend fun requestRegisterOtp(@Body request: OtpRequest): OtpResponse

    @POST("auth/otp/login")
    suspend fun requestLoginOtp(@Body request: OtpRequest): OtpResponse

    @GET("auth/me")
    suspend fun me(): UserProfileResponse

    @GET("users/profile/{username}")
    suspend fun getProfile(@Path("username") username: String): UserProfileResponse

    @GET("wallet/{address}/unified-balance")
    suspend fun unifiedBalance(@Path("address") smartAccountAddress: String): UnifiedBalanceResponse

    @POST("ai/parse-intent")
    suspend fun parseIntent(@Body request: ParseIntentRequest): AiIntentResponse

    @POST("payments/quote")
    suspend fun quotePayment(@Body request: QuotePaymentRequest): PaymentQuoteResponse

    @POST("payments/send")
    suspend fun sendPayment(@Body request: SendPaymentRequest): SendPaymentResponse

    @POST("payments/request")
    suspend fun createPaymentRequest(@Body request: CreatePaymentRequestBody): PaymentRequestResponse

    @GET("payments/inbox")
    suspend fun inbox(@Query("userId") userId: String): List<PaymentRequestResponse>

    @PATCH("payments/request/{id}")
    suspend fun resolvePaymentRequest(@Path("id") id: String, @Body request: ResolvePaymentRequestBody): PaymentRequestResponse

    @GET("activity")
    suspend fun activity(@Query("userId") userId: String): List<TransactionResponse>

    @POST("compliance/screen-counterparty")
    suspend fun screenCounterparty(@Body request: ScreenCounterpartyRequest): ComplianceVerdictResponse

    @GET("agents/marketplace")
    suspend fun agentMarketplace(): AgentMarketplaceResponse

    @POST("agents/wallets")
    suspend fun createAgentWallet(@Body request: CreateAgentWalletRequest): AgentWalletResponse

    @GET("agents/wallets")
    suspend fun listAgentWallets(): List<AgentWalletResponse>

    @GET("agents/wallets/{id}")
    suspend fun getAgentWallet(@Path("id") id: String): AgentWalletResponse

    @PATCH("agents/wallets/{id}")
    suspend fun updateAgentWallet(@Path("id") id: String, @Body request: UpdateAgentWalletRequest): AgentWalletResponse

    @DELETE("agents/wallets/{id}")
    suspend fun deleteAgentWallet(@Path("id") id: String)

    @GET("reputation/me")
    suspend fun myReputation(): ReputationResponse

    @GET("reputation/leaderboard")
    suspend fun reputationLeaderboard(@Query("limit") limit: Int = 10): List<LeaderboardEntry>

    @POST("merchants")
    suspend fun createMerchant(@Body request: CreateMerchantRequest): MerchantAccountResponse

    @GET("merchants/{id}/dashboard")
    suspend fun merchantDashboard(@Path("id") merchantId: String): MerchantDashboardResponse

    @POST("merchants/{id}/checkout-links")
    suspend fun createCheckoutLink(
        @Path("id") merchantId: String,
        @Body request: CreateCheckoutLinkRequest
    ): CheckoutLinkResponse

    @GET("subscriptions")
    suspend fun subscriptions(@Query("userId") userId: String): List<SubscriptionResponse>

    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): SubscriptionResponse

    @POST("subscriptions/{id}/pause")
    suspend fun pauseSubscription(@Path("id") id: String): SubscriptionResponse

    @POST("subscriptions/{id}/renew")
    suspend fun renewSubscription(@Path("id") id: String): SubscriptionResponse

    @POST("subscriptions/{id}/cancel")
    suspend fun cancelSubscription(@Path("id") id: String): SubscriptionResponse
}

data class RegisterStartRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val username: String
)

data class RegisterStartResponse(
    val message: String,
    val email: String,
    val displayName: String,
    val username: String,
    val passwordHash: String
)

data class RegisterVerifyRequest(
    val email: String,
    val code: String,
    val passwordHash: String,
    val displayName: String,
    val username: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class OtpRequest(
    val email: String
)

data class OtpResponse(
    val message: String
)

data class AuthSessionResponse(
    val user: UserProfileResponse,
    val session: SessionResponse
)

data class UserProfileResponse(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val smartAccountAddress: String,
    val reputationScore: Int,
    val isVerified: Boolean,
    val profileImageUrl: String? = null
)

data class SessionResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtEpochMillis: Long? = null
)

data class UnifiedBalanceResponse(
    val address: String,
    val chain: String,
    val chainId: Long,
    val balances: List<TokenBalanceResponse> = emptyList(),
    val total: String,
    val token: String,
    val isNativeUsdc: Boolean = false,
    val source: String? = null,
    val updatedAt: String? = null,
    val note: String? = null
)

data class TokenBalanceResponse(
    val symbol: String,
    val raw: String,
    val formatted: String,
    val decimals: Int,
    val contractAddress: String
)

data class ParseIntentRequest(
    val input: String
)

data class AiIntentResponse(
    val action: String,
    val amount: String? = null,
    val recipient: String? = null,
    val sourceChain: String? = null,
    val requiresConfirmation: Boolean = true,
    val confidence: Double = 0.0,
    val confirmationTitle: String? = null
)

data class QuotePaymentRequest(
    val fromAddress: String,
    val toAddress: String,
    val amount: String,
    val note: String? = null
)

data class PaymentQuoteResponse(
    val fromAddress: String,
    val toAddress: String,
    val amount: String,
    val token: String,
    val chain: String,
    val chainId: Long,
    val status: String,
    val estimatedFee: String,
    val feeToken: String,
    val note: String? = null,
    val signingProvider: String? = null,
    val message: String? = null,
    val relayerAddress: String? = null
)

data class SendPaymentRequest(
    val fromAddress: String,
    val toAddress: String,
    val amount: String,
    val note: String? = null
)

data class SendPaymentResponse(
    val id: String,
    val blockchainHash: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: String,
    val token: String,
    val chain: String,
    val chainId: Long,
    val status: String,
    val explorerUrl: String? = null,
    val message: String? = null
)

data class CreatePaymentRequestBody(
    val fromUserId: String,
    val toUserId: String,
    val amount: String,
    val note: String? = null
)

data class ResolvePaymentRequestBody(val status: String)

data class PaymentRequestResponse(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: String,
    val token: String,
    val note: String?,
    val status: String
)

data class ScreenCounterpartyRequest(
    val identifier: String,
    val amountUsd: String
)

data class ComplianceVerdictResponse(
    val allowed: Boolean,
    val riskScore: Int,
    val reason: String,
    val requiresKybOrKyc: Boolean,
    val source: String? = null,
    val checkedAt: String? = null
)

data class AgentMarketplaceResponse(
    val categories: List<String> = emptyList(),
    val agents: List<AgentListingResponse> = emptyList(),
    val settlementToken: String = "USDC",
    val network: String = "Arc Testnet",
    val policy: String? = null
)

data class AgentListingResponse(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val monthlyBudget: String,
    val token: String = "USDC",
    val reputationLabel: String,
    val riskLevel: String = "low",
    val permissions: List<String> = emptyList()
)

data class TransactionResponse(
    val id: String,
    val blockchainHash: String?,
    val amount: String,
    val token: String,
    val type: String,
    val status: String
)

data class CreateMerchantRequest(
    val ownerId: String,
    val businessName: String,
    val merchantHandle: String,
    val settlementAddress: String
)

data class MerchantAccountResponse(
    val id: String,
    val ownerId: String,
    val businessName: String,
    val merchantHandle: String,
    val settlementAddress: String
)

data class MerchantDashboardResponse(
    val merchant: MerchantAccountResponse,
    val dailyVolume: String,
    val weeklyVolume: String,
    val monthlyVolume: String,
    val token: String,
    val recentTransactions: List<TransactionResponse> = emptyList()
)

data class CreateCheckoutLinkRequest(
    val amount: String,
    val memo: String? = null,
    val customerReference: String? = null
)

data class CheckoutLinkResponse(
    val checkoutId: String,
    val merchantId: String,
    val businessName: String,
    val amount: String,
    val token: String,
    val payload: String,
    val checkoutUrl: String,
    val customerReference: String? = null,
    val status: String
)

data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val merchantId: String? = null,
    val agentWalletId: String? = null,
    val amount: String,
    val token: String,
    val interval: String,
    val status: String,
    val nextChargeAt: String? = null
)

data class CreateSubscriptionRequest(
    val userId: String,
    val merchantId: String? = null,
    val agentWalletId: String? = null,
    val amount: String,
    val interval: String,
    val nextChargeAt: String? = null
)

data class CreateAgentWalletRequest(
    val name: String,
    val description: String? = null,
    val monthlyBudget: String,
    val permissions: List<String> = emptyList()
)

data class UpdateAgentWalletRequest(
    val name: String? = null,
    val description: String? = null,
    val monthlyBudget: String? = null,
    val permissions: List<String>? = null
)

data class AgentWalletResponse(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String? = null,
    val walletAddress: String,
    val monthlyBudget: String,
    val permissions: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class ReputationResponse(
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
