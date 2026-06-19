package com.arcora.data.ai

import com.arcora.domain.ai.AiActionType
import com.arcora.domain.ai.AiAssistantRepository
import com.arcora.domain.ai.AiIntent
import com.arcora.domain.model.Money
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAiAssistantRepository @Inject constructor() : AiAssistantRepository {
    override suspend fun parseIntent(prompt: String): AiIntent {
        delay(500)
        val lower = prompt.lowercase()
        val amount = Regex("(\\d+(?:\\.\\d+)?)").find(lower)?.value?.let(Money::usdc)
        val recipient = Regex("@([a-z0-9_]+)").find(lower)?.value
        return when {
            lower.contains("send") -> AiIntent(
                action = AiActionType.SendPayment,
                amount = amount,
                recipient = recipient,
                confidence = 0.91,
                confirmationTitle = "Send ${amount?.formatted() ?: "USDC"} to ${recipient ?: "recipient"}"
            )
            lower.contains("request") -> AiIntent(
                action = AiActionType.RequestPayment,
                amount = amount,
                recipient = recipient,
                confidence = 0.88,
                confirmationTitle = "Request ${amount?.formatted() ?: "USDC"}"
            )
            lower.contains("move") || lower.contains("bridge") -> AiIntent(
                action = AiActionType.BridgeToArc,
                amount = amount,
                sourceChain = if (lower.contains("base")) "Base Sepolia" else "Ethereum Sepolia",
                confidence = 0.86,
                confirmationTitle = "Move funds to Arc"
            )
            lower.contains("spending") || lower.contains("spent") -> AiIntent(
                action = AiActionType.ShowSpending,
                confidence = 0.84,
                requiresConfirmation = false,
                confirmationTitle = "Show spending summary"
            )
            else -> AiIntent(AiActionType.Unknown, confidence = 0.2, requiresConfirmation = false, confirmationTitle = "I need more details")
        }
    }
}
