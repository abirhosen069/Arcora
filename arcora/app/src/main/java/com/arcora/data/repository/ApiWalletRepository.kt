package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.SendPaymentRequest
import com.arcora.data.api.TokenBalanceResponse
import com.arcora.data.api.TransactionResponse
import com.arcora.data.api.UserProfileResponse
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.model.BridgeQuote
import com.arcora.domain.model.ChainBalance
import com.arcora.domain.model.Money
import com.arcora.domain.model.PaymentRecipient
import com.arcora.domain.model.Portfolio
import com.arcora.domain.model.TransactionRecord
import com.arcora.domain.model.TransactionStatus
import com.arcora.domain.model.TransactionType
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiWalletRepository @Inject constructor(
    private val api: ArcOraApi,
    private val authRepository: AuthRepository
) : WalletRepository {
    private val portfolio = MutableStateFlow(
        Portfolio(
            totalBalance = Money.usdc("0"),
            availableOnArc = Money.usdc("0"),
            balances = emptyList()
        )
    )
    private val activity = MutableStateFlow<List<TransactionRecord>>(emptyList())

    override fun observePortfolio() = portfolio.asStateFlow()
    override fun observeActivity() = activity.asStateFlow()

    override suspend fun refreshPortfolio(smartAccountAddress: String): Portfolio = mapApiErrors {
        val response = api.unifiedBalance(smartAccountAddress)
        val liveArcBalance = Money.usdc(response.total)
        val updated = Portfolio(
            totalBalance = liveArcBalance,
            availableOnArc = liveArcBalance,
            balances = response.balances.ifEmpty {
                listOf(
                    TokenBalanceResponse(
                        symbol = response.token,
                        raw = "0",
                        formatted = response.total,
                        decimals = 18,
                        contractAddress = if (response.isNativeUsdc) "native" else "unknown"
                    )
                )
            }.map { balance ->
                ChainBalance(
                    chainName = response.chain,
                    chainKey = "Arc_Testnet",
                    balance = Money.usdc(balance.formatted),
                    isArcNative = true
                )
            }
        )
        portfolio.value = updated
        updated
    }

    override suspend fun resolveRecipient(input: String): PaymentRecipient = mapApiErrors {
        val normalized = if (input.startsWith("@")) input.lowercase() else "@${input.lowercase()}"
        val profile = api.getProfile(normalized)
        profile.toRecipient()
    }

    override suspend fun sendPayment(recipient: PaymentRecipient, amount: Money, note: String): TransactionRecord = mapApiErrors {
        val fromAddress = authRepository.currentUser.value?.smartAccountAddress
            ?: error("Sign in to send payments.")

        val response = api.sendPayment(
            SendPaymentRequest(
                fromAddress = fromAddress,
                toAddress = recipient.smartAccountAddress,
                amount = amount.amount.toPlainString(),
                note = note.ifBlank { null }
            )
        )

        TransactionRecord(
            id = response.id,
            title = "Sent to ${recipient.username}",
            subtitle = response.explorerUrl ?: response.blockchainHash,
            amount = amount,
            type = TransactionType.PaymentSent,
            status = when (response.status.lowercase()) {
                "completed" -> TransactionStatus.Completed
                "failed" -> TransactionStatus.Failed
                else -> TransactionStatus.Pending
            },
            createdAtLabel = "Just now"
        ).also { record ->
            activity.value = listOf(record) + activity.value
            authRepository.currentUser.value?.id?.let { refreshActivity(it) }
        }
    }

    override suspend fun createBridgeQuote(sourceChain: String, amount: Money): BridgeQuote = mapApiErrors {
        val fromAddress = authRepository.currentUser.value?.smartAccountAddress
            ?: error("Sign in to create a bridge quote.")

        val quote = api.quotePayment(
            com.arcora.data.api.QuotePaymentRequest(
                fromAddress = fromAddress,
                toAddress = fromAddress,
                amount = amount.amount.toPlainString()
            )
        )

        BridgeQuote(
            sourceChain = sourceChain,
            amount = amount,
            estimatedTime = "2-5 minutes",
            routeSummary = "$sourceChain USDC → CCTP → Arc Testnet USDC",
            destinationChain = "Arc_Testnet",
            fee = quote.estimatedFee
        )
    }

    override suspend fun bridgeToArc(quote: BridgeQuote): TransactionRecord = mapApiErrors {
        val fromAddress = authRepository.currentUser.value?.smartAccountAddress
            ?: error("Sign in to execute bridge.")

        val response = api.sendPayment(
            SendPaymentRequest(
                fromAddress = fromAddress,
                toAddress = fromAddress,
                amount = quote.amount.amount.toPlainString(),
                note = "Bridge from ${quote.sourceChain}"
            )
        )

        TransactionRecord(
            id = response.id,
            title = "Bridge from ${quote.sourceChain}",
            subtitle = response.explorerUrl ?: response.blockchainHash,
            amount = quote.amount,
            type = TransactionType.BridgeCompleted,
            status = when (response.status.lowercase()) {
                "completed" -> TransactionStatus.Completed
                "failed" -> TransactionStatus.Failed
                else -> TransactionStatus.Pending
            },
            createdAtLabel = "Just now"
        ).also { record ->
            activity.value = listOf(record) + activity.value
            authRepository.currentUser.value?.id?.let { refreshActivity(it) }
        }
    }

    suspend fun refreshActivity(userId: String): List<TransactionRecord> = mapApiErrors {
        api.activity(userId).map { it.toDomain() }.also { activity.value = it }
    }

    private fun UserProfileResponse.toRecipient() = PaymentRecipient(
        displayName = displayName,
        username = username,
        smartAccountAddress = smartAccountAddress,
        isVerified = isVerified
    )

    private fun TransactionResponse.toDomain() = TransactionRecord(
        id = id,
        title = when (type.uppercase()) {
            "PAYMENT" -> "USDC payment"
            "REQUEST" -> "Payment request"
            "BRIDGE" -> "Bridge activity"
            "SWAP" -> "Swap activity"
            "SUBSCRIPTION" -> "Subscription activity"
            else -> "ArcOra activity"
        },
        subtitle = blockchainHash ?: status,
        amount = Money.usdc(amount),
        type = when (type.uppercase()) {
            "REQUEST" -> TransactionType.RequestReceived
            "BRIDGE" -> TransactionType.BridgeCompleted
            "SWAP" -> TransactionType.SwapCompleted
            "SUBSCRIPTION" -> TransactionType.SubscriptionCharged
            else -> TransactionType.PaymentSent
        },
        status = when (status.uppercase()) {
            "COMPLETED" -> TransactionStatus.Completed
            "FAILED" -> TransactionStatus.Failed
            "REJECTED" -> TransactionStatus.Rejected
            else -> TransactionStatus.Pending
        },
        createdAtLabel = "Recent"
    )
}
