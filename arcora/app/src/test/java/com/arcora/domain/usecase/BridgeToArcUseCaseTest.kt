package com.arcora.domain.usecase

import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.compliance.ComplianceVerdict
import com.arcora.domain.model.BridgeQuote
import com.arcora.domain.model.Money
import com.arcora.domain.repository.WalletRepository
import com.arcora.domain.security.ApprovalResult
import com.arcora.domain.security.TransactionAuthorizer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BridgeToArcUseCaseTest {
    private lateinit var walletRepository: WalletRepository
    private lateinit var transactionAuthorizer: TransactionAuthorizer
    private lateinit var compliancePolicy: CompliancePolicy
    private lateinit var useCase: BridgeToArcUseCase

    private val fakeQuote = BridgeQuote(
        sourceChain = "Base Sepolia",
        amount = Money.usdc("100"),
        estimatedTime = "2-5 min",
        routeSummary = "Base → Arc"
    )

    @Before
    fun setup() {
        walletRepository = mockk()
        transactionAuthorizer = mockk()
        compliancePolicy = mockk()
        useCase = BridgeToArcUseCase(walletRepository, transactionAuthorizer, compliancePolicy)

        coEvery { walletRepository.createBridgeQuote(any(), any()) } returns fakeQuote
        coEvery { compliancePolicy.evaluateCounterparty(any(), any()) } returns ComplianceVerdict(
            allowed = true, riskScore = 10, reason = "OK", requiresKybOrKyc = false
        )
        coEvery { transactionAuthorizer.authorize(any()) } returns ApprovalResult.Approved
        coEvery { walletRepository.bridgeToArc(any()) } returns mockk()
    }

    @Test
    fun `quote returns bridge quote`() = runTest {
        val result = useCase.quote("Base Sepolia", Money.usdc("100"))
        assertEquals("Base Sepolia", result.sourceChain)
        assertEquals("100", result.amount.amount.toPlainString())
    }

    @Test
    fun `execute calls compliance, authorizer, and wallet repository`() = runTest {
        useCase.execute("Base Sepolia", Money.usdc("100"))
        coVerify { compliancePolicy.evaluateCounterparty("Base Sepolia", "100") }
        coVerify { transactionAuthorizer.authorize(any()) }
        coVerify { walletRepository.bridgeToArc(fakeQuote) }
    }

    @Test
    fun `execute throws when compliance blocks`() = runTest {
        coEvery { compliancePolicy.evaluateCounterparty(any(), any()) } returns ComplianceVerdict(
            allowed = false, riskScore = 95, reason = "Blocked", requiresKybOrKyc = true
        )
        try {
            useCase.execute("Base Sepolia", Money.usdc("100"))
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("blocked") == true)
        }
    }

    @Test
    fun `execute throws when user rejects`() = runTest {
        coEvery { transactionAuthorizer.authorize(any()) } returns ApprovalResult.Rejected
        try {
            useCase.execute("Base Sepolia", Money.usdc("100"))
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("rejected") == true)
        }
    }
}
