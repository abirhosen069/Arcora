package com.arcora.domain.model

data class ChainBalance(
    val chainName: String,
    val chainKey: String,
    val balance: Money,
    val isArcNative: Boolean = false
)

data class Portfolio(
    val totalBalance: Money,
    val availableOnArc: Money,
    val balances: List<ChainBalance>
)

enum class TransactionType {
    PaymentSent,
    PaymentReceived,
    RequestReceived,
    BridgeCompleted,
    SwapCompleted,
    SubscriptionCharged
}

enum class TransactionStatus {
    Pending,
    Completed,
    Failed,
    Rejected
}

data class TransactionRecord(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Money,
    val type: TransactionType,
    val status: TransactionStatus,
    val createdAtLabel: String
)

data class PaymentRecipient(
    val displayName: String,
    val username: String,
    val smartAccountAddress: String,
    val isVerified: Boolean
)

data class PaymentRequest(
    val id: String,
    val from: PaymentRecipient,
    val amount: Money,
    val note: String,
    val status: TransactionStatus
)

data class BridgeQuote(
    val sourceChain: String,
    val destinationChain: String = "Arc Testnet",
    val amount: Money,
    val estimatedTime: String,
    val routeSummary: String,
    val fee: String? = null
)
