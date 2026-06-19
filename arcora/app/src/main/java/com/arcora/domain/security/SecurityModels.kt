package com.arcora.domain.security

data class SessionToken(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtEpochMillis: Long? = null
)

data class TransactionApprovalRequest(
    val title: String,
    val subtitle: String,
    val amountLabel: String,
    val riskLevel: RiskLevel = RiskLevel.Low
)

enum class RiskLevel { Low, Medium, High }

sealed interface ApprovalResult {
    data object Approved : ApprovalResult
    data object Rejected : ApprovalResult
    data class Failed(val reason: String) : ApprovalResult
}
