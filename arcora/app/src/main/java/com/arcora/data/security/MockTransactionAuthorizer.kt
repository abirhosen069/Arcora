package com.arcora.data.security

import com.arcora.domain.security.ApprovalResult
import com.arcora.domain.security.TransactionApprovalRequest
import com.arcora.domain.security.TransactionAuthorizer
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTransactionAuthorizer @Inject constructor() : TransactionAuthorizer {
    override suspend fun authorize(request: TransactionApprovalRequest): ApprovalResult {
        // Production implementation will show Android BiometricPrompt in an Activity-bound adapter.
        delay(250)
        return ApprovalResult.Approved
    }
}
