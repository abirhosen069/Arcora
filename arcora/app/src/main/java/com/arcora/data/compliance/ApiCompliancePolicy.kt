package com.arcora.data.compliance

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.ScreenCounterpartyRequest
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.compliance.ComplianceVerdict
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiCompliancePolicy @Inject constructor(
    private val api: ArcOraApi
) : CompliancePolicy {
    override suspend fun evaluateCounterparty(identifier: String, amountUsd: String): ComplianceVerdict = mapApiErrors {
        val response = api.screenCounterparty(ScreenCounterpartyRequest(identifier, amountUsd))
        ComplianceVerdict(
            allowed = response.allowed,
            riskScore = response.riskScore,
            reason = response.reason,
            requiresKybOrKyc = response.requiresKybOrKyc
        )
    }
}
