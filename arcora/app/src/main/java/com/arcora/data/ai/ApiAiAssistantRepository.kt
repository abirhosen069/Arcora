package com.arcora.data.ai

import com.arcora.data.api.AiIntentResponse
import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.ParseIntentRequest
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.ai.AiActionType
import com.arcora.domain.ai.AiAssistantRepository
import com.arcora.domain.ai.AiIntent
import com.arcora.domain.model.Money
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAiAssistantRepository @Inject constructor(
    private val api: ArcOraApi
) : AiAssistantRepository {
    override suspend fun parseIntent(prompt: String): AiIntent = mapApiErrors {
        api.parseIntent(ParseIntentRequest(prompt)).toDomain()
    }

    private fun AiIntentResponse.toDomain() = AiIntent(
        action = when (action.uppercase()) {
            "SEND_PAYMENT" -> AiActionType.SendPayment
            "REQUEST_PAYMENT" -> AiActionType.RequestPayment
            "BRIDGE_TO_ARC" -> AiActionType.BridgeToArc
            "SHOW_SPENDING" -> AiActionType.ShowSpending
            else -> AiActionType.Unknown
        },
        amount = amount?.let(Money::usdc),
        recipient = recipient,
        sourceChain = sourceChain,
        confidence = confidence,
        requiresConfirmation = requiresConfirmation,
        confirmationTitle = confirmationTitle ?: defaultTitle()
    )

    private fun AiIntentResponse.defaultTitle(): String = when (action.uppercase()) {
        "SEND_PAYMENT" -> "Send ${amount ?: "USDC"} to ${recipient ?: "recipient"}"
        "REQUEST_PAYMENT" -> "Request ${amount ?: "USDC"}"
        "BRIDGE_TO_ARC" -> "Move funds to Arc"
        "SHOW_SPENDING" -> "Show spending summary"
        else -> "I need more details"
    }
}
