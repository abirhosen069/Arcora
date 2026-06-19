package com.arcora.domain.usecase

import com.arcora.domain.model.Money
import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.repository.WalletRepository
import com.arcora.domain.security.ApprovalResult
import com.arcora.domain.security.TransactionApprovalRequest
import com.arcora.domain.security.TransactionAuthorizer
import javax.inject.Inject

class SendPaymentUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionAuthorizer: TransactionAuthorizer,
    private val compliancePolicy: CompliancePolicy
) {
    suspend operator fun invoke(recipientInput: String, amount: Money, note: String) = run {
        val recipient = walletRepository.resolveRecipient(recipientInput)
        val verdict = compliancePolicy.evaluateCounterparty(recipient.smartAccountAddress, amount.amount.toPlainString())
        if (!verdict.allowed) {
            error("Compliance pre-check blocked this payment: ${verdict.reason}")
        }
        when (val approval = transactionAuthorizer.authorize(
            TransactionApprovalRequest(
                title = "Send ${amount.formatted()}",
                subtitle = "To ${recipient.username} • Risk ${verdict.riskScore}/100",
                amountLabel = amount.formatted(),
                riskLevel = when {
                    verdict.riskScore >= 70 -> com.arcora.domain.security.RiskLevel.High
                    verdict.riskScore >= 35 -> com.arcora.domain.security.RiskLevel.Medium
                    else -> com.arcora.domain.security.RiskLevel.Low
                }
            )
        )) {
            ApprovalResult.Approved -> walletRepository.sendPayment(recipient, amount, note)
            ApprovalResult.Rejected -> error("Transaction approval rejected")
            is ApprovalResult.Failed -> error(approval.reason)
        }
    }
}
