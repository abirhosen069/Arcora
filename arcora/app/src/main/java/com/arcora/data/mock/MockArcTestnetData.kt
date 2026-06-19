package com.arcora.data.mock

import com.arcora.domain.model.ChainBalance
import com.arcora.domain.model.Money
import com.arcora.domain.model.PaymentRecipient
import com.arcora.domain.model.Portfolio
import com.arcora.domain.model.TransactionRecord
import com.arcora.domain.model.TransactionStatus
import com.arcora.domain.model.TransactionType

object MockArcTestnetData {
    val defaultPortfolio = Portfolio(
        totalBalance = Money.usdc("2450.80"),
        availableOnArc = Money.usdc("880.25"),
        balances = listOf(
            ChainBalance("Arc Testnet", "Arc_Testnet", Money.usdc("880.25"), isArcNative = true),
            ChainBalance("Base Sepolia", "Base_Sepolia", Money.usdc("920.00")),
            ChainBalance("Ethereum Sepolia", "Ethereum_Sepolia", Money.usdc("650.55"))
        )
    )

    val recipients = listOf(
        PaymentRecipient("Alex Morgan", "@alex", "0xA1ex000000000000000000000000000000000001", true),
        PaymentRecipient("Sarah Chen", "@sarah", "0x5arah00000000000000000000000000000000002", true),
        PaymentRecipient("Coffee Shop", "@coffee", "0xC0ffee000000000000000000000000000000003", true)
    )

    val activity = listOf(
        TransactionRecord("tx_1", "Payment received", "From @alex", Money.usdc("120.00"), TransactionType.PaymentReceived, TransactionStatus.Completed, "Today"),
        TransactionRecord("tx_2", "Bridge completed", "Base Sepolia → Arc Testnet", Money.usdc("350.00"), TransactionType.BridgeCompleted, TransactionStatus.Completed, "Yesterday"),
        TransactionRecord("tx_3", "Coffee Shop", "Paid with ArcOra QR", Money.usdc("8.50"), TransactionType.PaymentSent, TransactionStatus.Completed, "May 31"),
        TransactionRecord("tx_4", "Sarah requested", "Dinner split", Money.usdc("24.00"), TransactionType.RequestReceived, TransactionStatus.Pending, "May 29")
    )
}
