package com.arcora.domain.security

interface TransactionAuthorizer {
    suspend fun authorize(request: TransactionApprovalRequest): ApprovalResult
}
