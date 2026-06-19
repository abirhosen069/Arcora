package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.mock.MockArcTestnetData
import com.arcora.domain.model.BridgeQuote
import com.arcora.domain.model.Money
import com.arcora.domain.model.PaymentRecipient
import com.arcora.domain.model.Portfolio
import com.arcora.domain.model.TransactionRecord
import com.arcora.domain.model.TransactionStatus
import com.arcora.domain.model.TransactionType
import com.arcora.domain.repository.WalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockWalletRepository @Inject constructor(
    private val api: ArcOraApi
) : WalletRepository {
    private val portfolio = MutableStateFlow(MockArcTestnetData.defaultPortfolio)
    private val activity = MutableStateFlow(MockArcTestnetData.activity)

    override fun observePortfolio() = portfolio.asStateFlow()
    override fun observeActivity() = activity.asStateFlow()

    override suspend fun refreshPortfolio(smartAccountAddress: String): Portfolio {
        val response = api.unifiedBalance(smartAccountAddress)
        val liveArcBalance = Money.usdc(response.total)
        val updated = Portfolio(
            totalBalance = liveArcBalance,
            availableOnArc = liveArcBalance,
            balances = listOf(
                com.arcora.domain.model.ChainBalance(
                    chainName = response.chain,
                    chainKey = "Arc_Testnet",
                    balance = liveArcBalance,
                    isArcNative = true
                )
            )
        )
        portfolio.value = updated
        return updated
    }

    override suspend fun resolveRecipient(input: String): PaymentRecipient {
        delay(250)
        val normalized = if (input.startsWith("@")) input.lowercase() else "@${input.lowercase()}"
        return MockArcTestnetData.recipients.firstOrNull { it.username == normalized }
            ?: PaymentRecipient(input.removePrefix("@"), normalized, "0xResolvedDemoRecipient000000000000000000000", false)
    }

    override suspend fun sendPayment(recipient: PaymentRecipient, amount: Money, note: String): TransactionRecord {
        delay(900)
        return TransactionRecord(
            id = "tx_send_${System.currentTimeMillis()}",
            title = "Sent to ${recipient.username}",
            subtitle = note.ifBlank { "Arc Testnet USDC payment" },
            amount = amount,
            type = TransactionType.PaymentSent,
            status = TransactionStatus.Completed,
            createdAtLabel = "Just now"
        ).also { activity.value = listOf(it) + activity.value }
    }

    override suspend fun createBridgeQuote(sourceChain: String, amount: Money): BridgeQuote {
        delay(400)
        return BridgeQuote(
            sourceChain = sourceChain,
            amount = amount,
            estimatedTime = "~4 minutes",
            routeSummary = "$sourceChain USDC → CCTP → Arc_Testnet USDC"
        )
    }

    override suspend fun bridgeToArc(quote: BridgeQuote): TransactionRecord {
        delay(1200)
        return TransactionRecord(
            id = "tx_bridge_${System.currentTimeMillis()}",
            title = "Bridge completed",
            subtitle = "${quote.sourceChain} → ${quote.destinationChain}",
            amount = quote.amount,
            type = TransactionType.BridgeCompleted,
            status = TransactionStatus.Completed,
            createdAtLabel = "Just now"
        ).also { activity.value = listOf(it) + activity.value }
    }
}
