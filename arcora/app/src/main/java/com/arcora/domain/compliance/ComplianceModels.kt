package com.arcora.domain.compliance

data class ComplianceVerdict(
    val allowed: Boolean,
    val riskScore: Int,
    val reason: String,
    val requiresKybOrKyc: Boolean
)

interface CompliancePolicy {
    suspend fun evaluateCounterparty(identifier: String, amountUsd: String): ComplianceVerdict
}
