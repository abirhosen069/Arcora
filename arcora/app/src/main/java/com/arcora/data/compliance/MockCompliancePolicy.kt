package com.arcora.data.compliance

import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.compliance.ComplianceVerdict
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockCompliancePolicy @Inject constructor() : CompliancePolicy {
    override suspend fun evaluateCounterparty(identifier: String, amountUsd: String): ComplianceVerdict {
        val amount = amountUsd.toBigDecimalOrNull() ?: return ComplianceVerdict(
            allowed = false,
            riskScore = 100,
            reason = "Invalid amount",
            requiresKybOrKyc = false
        )

        return when {
            amount > BigDecimal("10000") -> ComplianceVerdict(
                allowed = true,
                riskScore = 45,
                reason = "High-value transaction - standard compliance check passed",
                requiresKybOrKyc = true
            )
            amount > BigDecimal("1000") -> ComplianceVerdict(
                allowed = true,
                riskScore = 25,
                reason = "Medium-value transaction - compliance check passed",
                requiresKybOrKyc = false
            )
            else -> ComplianceVerdict(
                allowed = true,
                riskScore = 5,
                reason = "Low-value transaction - compliance check passed",
                requiresKybOrKyc = false
            )
        }
    }
}
