package com.arcora.domain.ai

import com.arcora.domain.model.Money

enum class AiActionType { SendPayment, RequestPayment, BridgeToArc, ShowSpending, Unknown }

data class AiIntent(
    val action: AiActionType,
    val amount: Money? = null,
    val recipient: String? = null,
    val sourceChain: String? = null,
    val confidence: Double = 0.0,
    val requiresConfirmation: Boolean = true,
    val confirmationTitle: String = "Review action"
)

interface AiAssistantRepository {
    suspend fun parseIntent(prompt: String): AiIntent
}
