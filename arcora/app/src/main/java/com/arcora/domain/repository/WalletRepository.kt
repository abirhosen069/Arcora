package com.arcora.domain.repository

import com.arcora.domain.model.BridgeQuote
import com.arcora.domain.model.Money
import com.arcora.domain.model.PaymentRecipient
import com.arcora.domain.model.Portfolio
import com.arcora.domain.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observePortfolio(): Flow<Portfolio>
    fun observeActivity(): Flow<List<TransactionRecord>>
    suspend fun refreshPortfolio(smartAccountAddress: String): Portfolio
    suspend fun resolveRecipient(input: String): PaymentRecipient
    suspend fun sendPayment(recipient: PaymentRecipient, amount: Money, note: String): TransactionRecord
    suspend fun createBridgeQuote(sourceChain: String, amount: Money): BridgeQuote
    suspend fun bridgeToArc(quote: BridgeQuote): TransactionRecord
}
