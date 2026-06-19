package com.arcora.domain.usecase

import com.arcora.domain.model.Money
import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.repository.WalletRepository
import com.arcora.domain.security.ApprovalResult
import com.arcora.domain.security.TransactionApprovalRequest
import com.arcora.domain.security.TransactionAuthorizer
import javax.inject.Inject

class BridgeToArcUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionAuthorizer: TransactionAuthorizer,
    private val compliancePolicy: CompliancePolicy
) {
    suspend fun quote(sourceChain: String, amount: Money) = walletRepository.createBridgeQuote(sourceChain, amount)
    suspend fun execute(sourceChain: String, amount: Money) = run {
        val quote = quote(sourceChain, amount)
        val verdict = compliancePolicy.evaluateCounterparty(sourceChain, amount.amount.toPlainString())
        if (!verdict.allowed) {
            error("Compliance pre-check blocked this bridge: ${verdict.reason}")
        }
        when (val approval = transactionAuthorizer.authorize(
            TransactionApprovalRequest(
                title = "Bridge ${amount.formatted()}",
                subtitle = "${quote.sourceChain} → ${quote.destinationChain} • Risk ${verdict.riskScore}/100",
                amountLabel = amount.formatted(),
                riskLevel = when {
                    verdict.riskScore >= 70 -> com.arcora.domain.security.RiskLevel.High
                    verdict.riskScore >= 35 -> com.arcora.domain.security.RiskLevel.Medium
                    else -> com.arcora.domain.security.RiskLevel.Low
                }
            )
        )) {
            ApprovalResult.Approved -> walletRepository.bridgeToArc(quote)
            ApprovalResult.Rejected -> error("Bridge approval rejected")
            is ApprovalResult.Failed -> error(approval.reason)
        }
    }
}
